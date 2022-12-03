package by.bsuir.instrumental.ftp.tftp;


import by.bsuir.instrumental.ftp.tftp.file.abort.AbortStructure;
import by.bsuir.instrumental.ftp.tftp.file.ack.AckStructure;
import by.bsuir.instrumental.ftp.tftp.file.block.table.BlockTable;
import by.bsuir.instrumental.ftp.tftp.file.block.table.portion.Portion;
import by.bsuir.instrumental.ftp.tftp.file.decline.DeclineStructure;
import by.bsuir.instrumental.ftp.tftp.file.input.FileInputStructure;
import by.bsuir.instrumental.ftp.tftp.file.output.FileOutputStructure;
import by.bsuir.instrumental.ftp.tftp.packet.type.TftpPacketType;
import by.bsuir.instrumental.ftp.util.file.FileBlockIOUtil;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.pool.SearchableQueuePool;
import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.slftp.dto.PortionRequest;
import by.bsuir.instrumental.ftp.slftp.meta.FileCopyProcess;
import by.bsuir.instrumental.ftp.slftp.meta.InputFileRecord;
import by.bsuir.instrumental.ftp.slftp.packet.type.SlftpPacketType;
import by.bsuir.instrumental.ftp.tftp.file.greating.GreatingStructure;
import by.bsuir.instrumental.ftp.tftp.file.nack.NackStructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static by.bsuir.instrumental.ftp.util.serialization.BodySerializer.deserializeBody;
import static by.bsuir.instrumental.ftp.util.serialization.BodySerializer.serializeBody;

@Slf4j
@RequiredArgsConstructor
public class TftpController {
    private static final short MAX_DECLINED_TIME = 3;
    private static final int PORTION_SIZE = 1024 << 1;
    private static final int MAX_IDEL_TIME = 512;
    private static final int MAX_BLOCK_SIZE = 128;
    private static int idelTime = 0;
    private final IdentificationHolder holder;
    private final SearchableQueuePool<String, FileOutputStructure> stringFileOutputStructureSearchableQueuePool;
    private final SearchableQueuePool<String, FileInputStructure> stringFileInputStructureSearchableQueuePool;
    private final Queue<Packet> packetQueue = new LinkedList<>();

    private static final String DOWNLOAD_DIRECTORY_PATH = "./downloads";

    public void handleRequest(Packet packet) {
        if (packet.getType() != PacketType.TFTP_PACKAGE.typeId) {
            throw new RuntimeException("slftp controller got non slftp packet");
        }
        switch (TftpPacketType.getByTypeId(packet.getFlags())) {
            case GREETING -> handleGreeting(packet);
            case METADATA -> handleMetadata(packet);
            case ACK -> handleAck(packet);
            case NACK -> handleNack(packet);
            case ABORT -> handleAbort(packet);
            case PORTION -> handlePortion(packet);
            case DECLINE -> handleDecline(packet);
            default -> log.error("nothing great happened");
        }
    }

    private void handleMetadata(Packet packet) {
        FileMetaData metaData = deserializeBody(packet.getBody());
        long blockAmount = Math.ceilDiv(metaData.getSize(), PORTION_SIZE * MAX_BLOCK_SIZE);
        long portionAmount = Math.ceilDiv(metaData.getSize(), PORTION_SIZE);
        String path = generateInputFilePath(metaData);
        FileOutputStructure outputStructure = new FileOutputStructure(metaData, blockAmount, portionAmount, path);
        String id = generateId(metaData);
        outputStructure.setId(id);
        BlockTable blockTable = generateBlockTable(0, portionAmount);
        outputStructure.setBlockTable(blockTable);
        stringFileOutputStructureSearchableQueuePool.offer(outputStructure);
    }

    private void handleNack(Packet packet) {
        NackStructure nackStructure = deserializeBody(packet.getBody());
        BlockTable requestedBlockTable = nackStructure.getBlockTable();
        FileMetaData requestedFileMetadata = nackStructure.getFileMetaData();
        long requestedBlockNum = requestedBlockTable.getBlockId();
        Optional<FileInputStructure> optional = stringFileInputStructureSearchableQueuePool.find(generateId(requestedFileMetadata));
        List<Portion> portionsForSend = new LinkedList<>();
        if(optional.isPresent()){
            FileInputStructure fileInputStructure = optional.get();
            if(fileInputStructure.getBlockNum() != requestedBlockNum){
                loadBlock(fileInputStructure, requestedBlockNum);
            }
            List<Portion> block = fileInputStructure.getBlock();
            byte[] table = requestedBlockTable.getTable();
            for(int counter = 0; counter < requestedBlockTable.getBlockSize(); counter++){
                byte tableElement = table[counter];
                if(tableElement!=0){
                    for(int counter1 = 0; counter1 < 8; counter1++){
                        if(((tableElement >> counter1) & 0b1) != 0){
                            portionsForSend.add(block.get((counter << 3) + counter1));
                        }
                    }
                }
            }
            portionsForSend.forEach(portion -> sendPacket(serializeBody(portion), packet.getTargetId(), packet.getSourceId(), TftpPacketType.PORTION));
        }else {
            sendPacket(serializeBody("No such file processing"), packet.getTargetId(), packet.getSourceId(), TftpPacketType.DECLINE);
        }
    }

    private void loadBlock(FileInputStructure fileInputStructure, long requestedBlockNum) {
        List<Portion> newBlock = FileBlockIOUtil.readBlock(fileInputStructure.getPath(), requestedBlockNum);
        fileInputStructure.setBlockNum(requestedBlockNum);
        fileInputStructure.setBlock(newBlock);
    }

    private void handleAck(Packet packet) {
        AckStructure ackStructure = deserializeBody(packet.getBody());
        FileMetaData metaData = ackStructure.getMetaData();
        Optional<FileInputStructure> optional = stringFileInputStructureSearchableQueuePool.find(generateId(metaData));
        if(optional.isPresent()){
            nextTransition(packet, ackStructure, optional.get());
        }else {
            sendPacket(serializeBody("No such file processing"), packet.getTargetId(), packet.getSourceId(), TftpPacketType.DECLINE);
        }
    }

    private void handleDecline(Packet packet) {
        DeclineStructure declineStructure = deserializeBody(packet.getBody());
        log.info(declineStructure.getCode() + ": " + declineStructure.getMessage());
    }

    private void handleAbort(Packet packet) {
        AbortStructure abortStructure = deserializeBody(packet.getBody());
        stringFileInputStructureSearchableQueuePool.remove(generateId(abortStructure.getFileMetaData()));
        log.warn("abort " + abortStructure.getCode() + ": " + abortStructure.getMessage());
    }

    private void handlePortion(Packet packet) {
        Portion portion = deserializeBody(packet.getBody());
        Optional<FileOutputStructure> optional = stringFileOutputStructureSearchableQueuePool.find(portion.getId());
        if (optional.isPresent()) {
            FileOutputStructure fileOutputStructure = optional.get();
            String path = fileOutputStructure.getPath();
            BlockTable blockTable = fileOutputStructure.getBlockTable();
            byte[] blockTableContent = blockTable.getTable();
            List<Portion> block = fileOutputStructure.getPortions();
            long blockNum = fileOutputStructure.getBlockNum();
            int blockElementIndex = (portion.getPortionNum() >> 3);
            byte blockElementOffset = (byte) (0b1 << (portion.getPortionNum() & 0x7));

            blockTableContent[blockElementIndex] = (byte) (blockTableContent[blockElementIndex] & (~blockElementOffset));
            block.add(portion.getPortionNum(), portion);

            if(isBlockComplete(blockTable)){
                FileBlockIOUtil.writeBlock(path, blockNum, block);
                AckStructure ackStructure = new AckStructure(fileOutputStructure.getMetadata(), blockNum);
                sendPacket(serializeBody(ackStructure), holder.getIdentifier().getBytes(), packet.getSourceId(), TftpPacketType.ACK);
            }
        }
    }

    private void handleGreeting(Packet packet) {
        GreatingStructure greatingStructure = deserializeBody(packet.getBody());
        String targetId = new String(packet.getSourceId());
        initCommunicationWithFileName(greatingStructure.getPath(), targetId);
    }

    private boolean isBlockComplete(BlockTable blockTable) {
        byte[] table = blockTable.getTable();
        boolean isFull = true;
        for(int counter = 0; counter < table.length; counter ++){
            if(table[counter] != 0){
                isFull = false;
                break;
            }
        }
        return isFull;
    }

    private void nextTransition(Packet packet, AckStructure ackStructure, FileInputStructure fileInputStructure ) {
        long ackBlockNum = ackStructure.getBlockNum();
        if(fileInputStructure.getBlockNum() == ackStructure.getBlockNum()){
            if(fileInputStructure.getBlockAmount() <= ackBlockNum + 1){
                stringFileInputStructureSearchableQueuePool.remove(fileInputStructure.getId());
                log.info(fileInputStructure.getMetadata().getUrl() + " fully transmitted to " + fileInputStructure.getMetadata().getHostId());
            }else {
                loadBlock(fileInputStructure, ackBlockNum + 1);
                fileInputStructure.getBlock().forEach(portion -> sendPacket(serializeBody(portion), holder.getIdentifier().getBytes(), packet.getSourceId(), TftpPacketType.PORTION));
            }
        }
    }

    private static void addProcessMils(FileCopyProcess copyProcess) {
        copyProcess.setMils(copyProcess.getMils() + (System.currentTimeMillis() - copyProcess.getLastTimeTransceive()));
    }

    private static String generateId(FileMetaData metaData){
        return metaData.getHostId() + metaData.getUrl();
    }

    private static BlockTable generateBlockTable(int blockNum, long portionAmount){
        long offset = (long) blockNum * MAX_BLOCK_SIZE;
        long portionsRemaining = portionAmount - offset;
        int blockSize = portionsRemaining < MAX_BLOCK_SIZE ? (int) portionsRemaining : MAX_BLOCK_SIZE;
        int tableDataSize = blockSize>>3 + 1;
        int lastTableElementBitsMask = blockSize & 0x7;
        byte[] tableData = new byte[tableDataSize];

        for(int counter = 0; counter < tableDataSize - 1; counter ++){
            tableData[counter] = (byte) 0xff;
        }
        for(int counter = 0; counter < lastTableElementBitsMask; counter++){
            tableData[tableDataSize - 1] |= 0b1 << counter;
        }

        BlockTable blockTable = new BlockTable();
        blockTable.setBlockSize(blockSize);
        blockTable.setBlockId(blockNum);
        blockTable.setTable(tableData);

        return blockTable;
    }

    private List<Portion> generateBlock(FileInputStructure fileInputStructure, BlockTable blockTable){
        List<Portion> block = new LinkedList<>();
        long blockNum = blockTable.getBlockId();
        byte[] blockTableData = blockTable.getTable();
        for(int counter = 0; counter < blockTableData.length; counter++){
            byte portionsMask = blockTableData[counter];
            if(portionsMask != 0){
                long offset = blockNum * MAX_BLOCK_SIZE + counter << 3;
                for(int counter1 = 0; counter1 < 8; counter1 ++){
                    if((portionsMask & (0b1 << counter1)) != 0){
                        Portion portion = generatePortion(offset + counter1, counter << 3 + counter1);
                        block.add(portion);
                    }
                }
            }
        }
        return block;
    }

    private Portion generatePortion(long portionAbsoluteNum, int portionBlockNum){
        byte[] portionContent = getPortionContent(portionAbsoluteNum);
        Portion portion = new Portion()
                .
                .// todo вынеси эту херь в отдельный класс, а то скоро голова нахрен расплавится от этого протокола
    }
    private static String generateInputFilePath(FileMetaData metaData){
        return DOWNLOAD_DIRECTORY_PATH + getFileName(metaData.getUrl());
    }

    private static PortionRequest getPortionRequestFromCopy(FileCopyProcess copyProcess) {
        return new PortionRequest()
                .setFileUri(copyProcess.getMetaData().getUrl())
                .setHostId(copyProcess.getMetaData().getHostId())
                .setPortion(copyProcess.getPortion());
    }

    private static void closeClosable(FileCopyProcess copyProcess) {
        try {
            copyProcess.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Packet receive() {
        Packet packet = packetQueue.poll();
        if (packet == null) {
            ++idelTime;
        } else {
            idelTime = 0;
        }
        if (idelTime >= MAX_IDEL_TIME) {
            packet = restoreExistingProcess();
        }
        return packet;
    }

    private Packet restoreExistingProcess() {
        Packet packet = null;
        Optional<FileCopyProcess> optional = processSearchablePool.poll();
        if (optional.isPresent()) {
            FileCopyProcess copyProcess = optional.get();
            copyProcess.setLastTimeTransceive(System.currentTimeMillis());
            PortionRequest request = getPortionRequestFromCopy(copyProcess);
            sendPacket(serializeBody(request), holder.getIdentifier().getBytes(), copyProcess.getMetaData().getHostId().getBytes(), SlftpPacketType.PORTION_REQ);
            processSearchablePool.offer(copyProcess);
            packet = packetQueue.poll();
            idelTime = 0;
        }
        return packet;
    }

    public void initCommunicationWithFileName(String path, String destinationId) {
        Optional<FileMetaData> optional = getMetadataByFilePath(path);
        if(optional.isPresent()){
            FileMetaData metaData = optional.get();
            FileInputStructure fileInputStructure = new FileInputStructure(metaData, path);
            String id = generateId(metaData);
            fileInputStructure.setId(id);
            List<Portion> block = getBlock();
        }
        optional.ifPresent(fileMetaData -> sendPacket(serializeBody(fileMetaData), holder.getIdentifier().getBytes(), destinationId.getBytes(), TftpPacketType.METADATA));
    }

    public void requestCommunication(String uri, String destinationId) {
        Optional<FileMetaData> optional = Optional.of(new FileMetaData().setHostId(destinationId).setUrl(uri));
        optional.ifPresent(fileMetaData -> sendPacket(serializeBody(fileMetaData), holder.getIdentifier().getBytes(), destinationId.getBytes(), SlftpPacketType.GREETING_REQ));
    }

    private void sendPacket(byte[] body, byte[] sourceId, byte[] targetId, TftpPacketType decline) {
        Packet resultPacket = new Packet(body, sourceId, targetId, PacketType.SLFTP_PACKAGE.typeId, decline.typeId);
        packetQueue.offer(resultPacket);
    }

    private Portion readPortion(InputFileRecord inputFileRecord, PortionRequest request) throws IOException {
        FileInputStream inputStream = inputFileRecord.getStream();
        int bytesAvailable = inputStream.available();
        int length = Math.min(bytesAvailable, PORTION_SIZE);
        long offset = request.getPortion() * PORTION_SIZE;
        byte[] content = new byte[length];
        inputStream.getChannel().position(offset);
        int size = inputStream.read(content, 0, length);
        return new Portion()
                .setPortionNum(request.getPortion())
                .setFileUri(request.getFileUri())
                .setHostId(request.getHostId().getBytes())
                .setActualSize(size)
                .setContent(content);
    }

    private Optional<InputFileRecord> tryCreateRecord(PortionRequest request) throws FileNotFoundException {
        Optional<InputFileRecord> optional = Optional.empty();

        File file = new File(request.getFileUri());
        if (file.exists() && !file.isDirectory()) {
            FileInputStream inputStream = new FileInputStream(file);
            FileMetaData metaData = new FileMetaData()
                    .setUrl(request.getFileUri())
                    .setSize(file.length())
                    .setHostId(holder.getIdentifier());
            InputFileRecord fileRecord = new InputFileRecord()
                    .setMetaData(metaData)
                    .setStream(inputStream);
            inputFileRecordSearchablePool.offer(fileRecord);
            optional = Optional.of(fileRecord);
        }
        return optional;
    }

    private Optional<FileMetaData> getMetadataByFilePath(String url) {
        Optional<FileMetaData> optional = Optional.empty();
        File file = new File(url);
        if (file.exists() && !file.isDirectory()) {
            FileMetaData metaData = new FileMetaData()
                    .setUrl(url)
                    .setSize(file.length())
                    .setHostId(holder.getIdentifier());
            optional = Optional.of(metaData);
        }
        return optional;
    }

    private void writePortionToFile(FileCopyProcess copyProcess, Portion portion) {
        byte[] bytes = portion.getContent();
        int length = (int) portion.getActualSize();
        OutputStream outputStream = copyProcess.getStream();
        try {
            outputStream.write(bytes, 0, length);
            log.info("portion " + copyProcess.getPortion() + " of " + copyProcess.getMetaData().getSize() / PORTION_SIZE + " transferred" + copyProcess.getMetaData().getUrl());
            copyProcess.setPortion(copyProcess.getPortion() + 1);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileOutputStream openOutputStream(FileMetaData metaData) {
        createDownloadDirIfNotExist();
        File file = new File(DOWNLOAD_DIRECTORY_PATH + "/" + getFileName(metaData.getUrl()));
        file.delete();
        try {
            file.createNewFile();
            return new FileOutputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFileName(String fileUrl) {
        return Path.of(fileUrl).getFileName().toString();
    }

    private void createDownloadDirIfNotExist(){
        File directory = new File(DOWNLOAD_DIRECTORY_PATH);
        if(!directory.exists()||!directory.isDirectory()){
            directory.mkdirs();
        }

    }

}
