package by.bsuir.instrumental.ftp.tftp;


import by.bsuir.instrumental.ftp.FtpController;
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
import by.bsuir.instrumental.ftp.tftp.file.greating.GreatingStructure;
import by.bsuir.instrumental.ftp.tftp.file.nack.NackStructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static by.bsuir.instrumental.ftp.util.file.FileBlockIOUtil.getBlockAmount;
import static by.bsuir.instrumental.ftp.util.file.FileBlockIOUtil.getMetadataByFilePath;
import static by.bsuir.instrumental.ftp.util.serialization.BodySerializer.deserializeBody;
import static by.bsuir.instrumental.ftp.util.serialization.BodySerializer.serializeBody;

@Slf4j
@RequiredArgsConstructor
public class TftpController implements FtpController {
    private static final int PORTION_SIZE = 1024 << 1;
    private static final int MAX_IDLE_TIME = 512;
    private static final int MAX_BLOCK_SIZE = 128;
    private static int idleTime = 0;
    private final IdentificationHolder holder;
    private final SearchableQueuePool<String, FileOutputStructure> stringFileOutputStructureSearchableQueuePool;
    private final SearchableQueuePool<String, FileInputStructure> stringFileInputStructureSearchableQueuePool;
    private final LinkedList<Packet> packetQueue = new LinkedList<>();

    private static final String DOWNLOAD_DIRECTORY_PATH = "./downloads";

    @Override
    public List<Packet> receive() {
        if (packetQueue.isEmpty()) {
            ++idleTime;
        } else {
            idleTime = 0;
        }
        if (idleTime >= MAX_IDLE_TIME) {
            restoreExistingProcess();
        }
        List<Packet> packets = new ArrayList<>(packetQueue);
        packetQueue.clear();
        return packets;
    }

    @Override
    public void send(Packet packet) {
        if (packet.getType() != PacketType.FTP_PACKAGE.typeId) {
            throw new RuntimeException("tftp controller got non tftp packet");
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

    @Override
    public void upload(String path, String destinationId) {
        Optional<FileMetaData> optional = getMetadataByFilePath(path, holder.getIdentifier());
        if(optional.isPresent()){
            FileMetaData metaData = optional.get();
            String id = generateId(metaData);
            long blockAmount = getBlockAmount(metaData);
            FileInputStructure fileInputStructure = new FileInputStructure(id, metaData, path, blockAmount);
            stringFileInputStructureSearchableQueuePool.offer(fileInputStructure);
        }else {
            DeclineStructure declineStructure = new DeclineStructure("0", "no such file exists");
            sendPacket(serializeBody(declineStructure), holder.getIdentifier().getBytes(), destinationId.getBytes(), TftpPacketType.DECLINE);
        }
        optional.ifPresent(fileMetaData -> sendPacket(serializeBody(fileMetaData), holder.getIdentifier().getBytes(), destinationId.getBytes(), TftpPacketType.METADATA));
    }

    @Override
    public void download(String path, String sourceId){
        GreatingStructure greatingStructure = new GreatingStructure(path);
        sendPacket(serializeBody(greatingStructure), holder.getIdentifier().getBytes(), sourceId.getBytes(), TftpPacketType.GREETING);
    }

    private void handleMetadata(Packet packet) {
        FileMetaData metaData = deserializeBody(packet.getBody());
        long blockAmount = Math.ceilDiv(metaData.getSize(), PORTION_SIZE * MAX_BLOCK_SIZE);
        long portionAmount = Math.ceilDiv(metaData.getSize(), PORTION_SIZE);
        String path = generateInputFilePath(metaData);
        FileBlockIOUtil.normalizePath(path);
        String id = generateId(metaData);
        FileOutputStructure outputStructure = new FileOutputStructure(id, metaData, path, blockAmount, portionAmount);
        stringFileOutputStructureSearchableQueuePool.offer(outputStructure);
        NackStructure nackStructure = new NackStructure(metaData, outputStructure.getBlockTable());
        sendPacket(serializeBody(nackStructure), packet.getTargetId(), packet.getSourceId(), TftpPacketType.NACK);//todo testing is stopped on adding nack after metadata
    }

    private void handleNack(Packet packet) {
        NackStructure nackStructure = deserializeBody(packet.getBody());
        BlockTable requestedBlockTable = nackStructure.getBlockTable();
        FileMetaData requestedFileMetadata = nackStructure.getFileMetaData();
        long requestedBlockNum = requestedBlockTable.getBlockId();
        Optional<FileInputStructure> optional = stringFileInputStructureSearchableQueuePool.find(generateId(requestedFileMetadata));
        if(optional.isEmpty()){
            optional = tryCreateInput(requestedFileMetadata, requestedBlockTable);
        }
        List<Portion> portionsForSend = new LinkedList<>();
        if(optional.isPresent()){
            createDownloadDirIfNotExist();
            FileInputStructure fileInputStructure = optional.get();
            if(fileInputStructure.getBlockNum() != requestedBlockNum){// todo full inconsistency with unordered requests
                fileInputStructure.nextBlock();
            }// todo potentially unused section
            List<Portion> block = fileInputStructure.getBlock();
            byte[] table = requestedBlockTable.getTable();
            for(int counter = 0; counter < table.length; counter++){
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
            AbortStructure abortStructure = new AbortStructure(requestedFileMetadata, "0", "No such file processing");
            sendPacket(serializeBody(abortStructure), packet.getTargetId(), packet.getSourceId(), TftpPacketType.ABORT);
        }
    }

    private Optional<FileInputStructure> tryCreateInput(FileMetaData requestedFileMetadata, BlockTable requestedBlockTable) {
        String id = generateId(requestedFileMetadata);
        String path = DOWNLOAD_DIRECTORY_PATH + "/" + getFileName(requestedFileMetadata.getUrl());
        File file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long blockAmount = getBlockAmount(requestedFileMetadata);
        FileInputStructure structure = new FileInputStructure(id, requestedFileMetadata, path, blockAmount, requestedBlockTable);
        stringFileInputStructureSearchableQueuePool.offer(structure);
        return Optional.of(structure);
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
            if(portion.getBlockNum() == fileOutputStructure.getBlockNum()){
                String path = fileOutputStructure.getPath();
                BlockTable blockTable = fileOutputStructure.getBlockTable();
                byte[] blockTableContent = blockTable.getTable();
                List<Portion> block = fileOutputStructure.getPortions();
                long blockNum = fileOutputStructure.getBlockNum();
                int blockElementIndex = (portion.getPortionNum() >> 3);
                byte blockElementOffset = (byte) (0b1 << (portion.getPortionNum() & 0x7));
                if((blockTableContent[blockElementIndex] & (blockElementOffset)) != 0){
                    block.add(portion.getPortionNum(), portion);
                    blockTableContent[blockElementIndex] = (byte) (blockTableContent[blockElementIndex] & (~blockElementOffset));
                }
                if(blockTable.isComplete()){
                    fileOutputStructure.incBlockNum();
                    log.info("block " + blockNum + " had been transmitted");
                    if(fileOutputStructure.isComplete()){
                        stringFileOutputStructureSearchableQueuePool.remove(fileOutputStructure.getId());
                        log.info("file " + fileOutputStructure.getPath() + " had been transmitted");
                        log.info("Transition rate: " + (fileOutputStructure.getMetadata().getSize() * 8 * 1000) / (System.currentTimeMillis() - fileOutputStructure.getStartMils()) + " bit/s");
                    }
                    FileBlockIOUtil.writeBlock(path, block);
                    AckStructure ackStructure = new AckStructure(fileOutputStructure.getMetadata(), blockNum);
                    sendPacket(serializeBody(ackStructure), holder.getIdentifier().getBytes(), packet.getSourceId(), TftpPacketType.ACK);
                    log.info("block " + blockNum + " had been saved");
                }
            }
        }
    }

    private void handleGreeting(Packet packet) {
        GreatingStructure greatingStructure = deserializeBody(packet.getBody());
        String targetId = new String(packet.getSourceId());
        upload(greatingStructure.getPath(), targetId);
    }

    private void nextTransition(Packet packet, AckStructure ackStructure, FileInputStructure fileInputStructure) {
        long ackBlockNum = ackStructure.getBlockNum();
        if(fileInputStructure.getBlockNum() == ackStructure.getBlockNum()){
            if(fileInputStructure.getBlockAmount() <= ackBlockNum + 1){
                stringFileInputStructureSearchableQueuePool.remove(fileInputStructure.getId());
                log.info(fileInputStructure.getMetadata().getUrl() + " fully transmitted to " + fileInputStructure.getMetadata().getHostId());
            }else {
                fileInputStructure.nextBlock();
                fileInputStructure.getBlock().forEach(portion -> sendPacket(serializeBody(portion), holder.getIdentifier().getBytes(), packet.getSourceId(), TftpPacketType.PORTION));
                log.info("block " + fileInputStructure.getBlockNum() + " had been sent");
            }
        }
    }

    private static String generateId(FileMetaData metaData){
        return metaData.getHostId() + metaData.getUrl();
    }

    private static String generateInputFilePath(FileMetaData metaData){
        return DOWNLOAD_DIRECTORY_PATH + "/"+ getFileName(metaData.getUrl());
    }

    private void restoreExistingProcess() {
        Optional<FileOutputStructure> optional = stringFileOutputStructureSearchableQueuePool.poll();
        if (optional.isPresent()) {
            FileOutputStructure fileOutputStructure = optional.get();
            NackStructure nackStructure = new NackStructure(fileOutputStructure.getMetadata(), fileOutputStructure.getBlockTable());
            sendPacket(serializeBody(nackStructure), holder.getIdentifier().getBytes(), fileOutputStructure.getMetadata().getHostId().getBytes(), TftpPacketType.NACK);
            idleTime = 0;
        }
    }

    private void sendPacket(byte[] body, byte[] sourceId, byte[] targetId, TftpPacketType decline) {
        Packet resultPacket = new Packet(body, sourceId, targetId, PacketType.FTP_PACKAGE.typeId, decline.typeId);
        packetQueue.offer(resultPacket);
    }

    private static String getFileName(String fileUrl) {
        return Path.of(fileUrl).getFileName().toString();
    }

    private void createDownloadDirIfNotExist(){
        File directory = new File(DOWNLOAD_DIRECTORY_PATH);
        if(!directory.exists()||!directory.isDirectory()){
            if(directory.mkdirs()){
                throw new RuntimeException("can not create download directory");
            }
        }
    }
}
