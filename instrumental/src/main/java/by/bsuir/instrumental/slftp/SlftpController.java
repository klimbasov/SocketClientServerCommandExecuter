package by.bsuir.instrumental.slftp;

import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.pool.SearchableQueuePool;
import by.bsuir.instrumental.slftp.meta.FileCopyProcess;
import by.bsuir.instrumental.slftp.dto.FileMetaData;
import by.bsuir.instrumental.slftp.dto.Portion;
import by.bsuir.instrumental.slftp.dto.PortionRequest;
import by.bsuir.instrumental.slftp.meta.InputFileRecord;
import by.bsuir.instrumental.slftp.packet.type.SlftpPacketType;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.slftp.pool.FileProcessUriQueuePool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Slf4j
public class SlftpController {
    private final IdentificationHolder holder;
    private final FileProcessUriQueuePool processSearchablePool;
    private final SearchableQueuePool<String, InputFileRecord> inputFileRecordSearchablePool;
    private static final short MAX_DECLINED_TIME = 3;
    private static final int PORTION_SIZE = 1024;
    private final Queue<Packet> packetQueue = new LinkedList<>();

    public void handleRequest(Packet packet){
        if(packet.getType() != PacketType.SLFTP_PACKAGE.typeId){
            throw new RuntimeException("slftp controller got non slftp packet");
        }
        switch (SlftpPacketType.getByTypeId(packet.getFlags())){
            case GREETING -> handleGreeting(packet);
            case PORTION -> handlePortion(packet);
            case PORTION_REQ -> handlePortionReq(packet);
            case ABORT -> handleAbort(packet);
            case DECLINE -> hendleDecline(packet);
            case NOT_PASSED -> handleNotPassed(packet);
            default -> log.error("nothing great happened");
        }
    }

    private void handleNotPassed(Packet packet) {
        PortionRequest request = deserializeBody(packet.getBody());
        sendPacket(packet.getBody(), holder.getIdentifier().getBytes(), request.getHostId().getBytes(), SlftpPacketType.PORTION_REQ);
    }

    public Packet receive(){
        return packetQueue.poll();
    }

    public void initCommunicationWithFileName(String uri, String destinationId){
        Optional<FileMetaData> optional = getMetadataByFileUrl(uri);
        optional.ifPresent(fileMetaData -> sendPacket(serializeBody(fileMetaData), holder.getIdentifier().getBytes(), destinationId.getBytes(), SlftpPacketType.GREETING));
    }

    private void hendleDecline(Packet packet) {
        PortionRequest request = deserializeBody(packet.getBody());
        Optional<FileCopyProcess> optional = processSearchablePool.find(request.getFileUri());
        if(optional.isPresent()){
            FileCopyProcess copyProcess = optional.get();
            copyProcess.setTimesDeclined((short) (copyProcess.getTimesDeclined()+ 1));
            if(copyProcess.getTimesDeclined() == MAX_DECLINED_TIME){
                FileMetaData metaData = copyProcess.getMetaData();
                processSearchablePool.remove(metaData.getUrl());
                
                log.info("file " + metaData.getUrl() + " from host " + metaData.getHostId() + " was not loaded");
            }else {
                sendPacket(packet.getBody(), packet.getTargetId(), packet.getSourceId(), SlftpPacketType.PORTION_REQ);
            }
        }
    }

    private void handleAbort(Packet packet) {
        FileMetaData metaData = deserializeBody(packet.getBody());
        inputFileRecordSearchablePool.remove(metaData.getUrl());
    }

    private void handlePortionReq(Packet packet) {
        PortionRequest request = deserializeBody(packet.getBody());
        Optional<InputFileRecord> record = inputFileRecordSearchablePool.find(request.getFileUri());
        try {
            if(record.isEmpty()){
                record = tryCreateRecord(request);
            }
            if(record.isPresent()){
                Portion portion = readPortion(record.get(), request);
                sendPacket(serializeBody(portion), packet.getTargetId(), packet.getSourceId(), SlftpPacketType.PORTION);
            }else {
                sendPacket(packet.getBody(), packet.getTargetId(), packet.getSourceId(), SlftpPacketType.DECLINE);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    //стз
    private void handlePortion(Packet packet) {
        Portion portion = deserializeBody(packet.getBody());
        Optional<FileCopyProcess> optional = processSearchablePool.find(portion.getFileUri());
        if(optional.isPresent()){
            FileCopyProcess copyProcess = optional.get();
            if(copyProcess.getPortion() == portion.getPortionNum()){
                writePortionToFile(copyProcess, portion);
            }
            if(copyProcess.getPortion() == copyProcess.getPortionsQuantity()){
                log.info("transferring " + copyProcess.getMetaData().getUrl() + " finished. Time consumed " + (System.currentTimeMillis() - copyProcess.getMils()));
                sendPacket(serializeBody(copyProcess.getMetaData()), packet.getTargetId(), packet.getSourceId(), SlftpPacketType.ABORT);
                processSearchablePool.remove(copyProcess.getMetaData().getUrl());
                closeClosable(copyProcess);
            }else {
                PortionRequest request = getPortionRequestFromCopy(copyProcess);
                sendPacket(serializeBody(request), holder.getIdentifier().getBytes(), copyProcess.getMetaData().getHostId().getBytes(), SlftpPacketType.PORTION_REQ);
            }
        }
    }

    private void sendPacket(byte[] packet, byte[] sourceId, byte[] targetId, SlftpPacketType decline) {
        Packet resultPacket = new Packet(packet, sourceId, targetId, PacketType.SLFTP_PACKAGE.typeId, decline.typeId);
        packetQueue.offer(resultPacket);
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

    private void handleGreeting(Packet packet) {
        FileMetaData metaData = deserializeBody(packet.getBody());
        long portionsQuantity = getPortionsQuantity(metaData);
        FileCopyProcess  copyProcess = processSearchablePool.find(metaData.getUrl()).orElse(null);

        if(notAlreadyLoading(metaData, copyProcess)){
            FileOutputStream outputStream = openOutputStream(metaData);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            copyProcess = new FileCopyProcess()
                    .setMils(System.currentTimeMillis())
                    .setMetaData(metaData)
                    .setPortion(0)
                    .setPortionsQuantity(portionsQuantity)
                    .setStream(bufferedOutputStream);
            processSearchablePool.offer(copyProcess);
            PortionRequest request = getPortionRequestFromCopy(copyProcess);
            sendPacket(serializeBody(request), holder.getIdentifier().getBytes(), copyProcess.getMetaData().getHostId().getBytes(), SlftpPacketType.PORTION_REQ);
        }
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
        if(file.exists() && !file.isDirectory()){
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

    private Optional<FileMetaData> getMetadataByFileUrl(String url){
        Optional<FileMetaData> optional = Optional.empty();
        File file = new File(url);
        if(file.exists() && !file.isDirectory()){
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
            outputStream.write(bytes, 0,  length);
            log.info("portion " + copyProcess.getPortion() + " of " + copyProcess.getMetaData().getSize()/PORTION_SIZE + " transferred" + copyProcess.getMetaData().getUrl());
            copyProcess.setPortion(copyProcess.getPortion()+1);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long getPortionsQuantity(FileMetaData metaData) {
        return (metaData.getSize() + (PORTION_SIZE - 1)) / PORTION_SIZE;
    }

    private FileOutputStream openOutputStream(FileMetaData metaData) {
        File file = new File(getFileName(metaData.getUrl()));
        file.delete();
        try {
            file.createNewFile();
            return new FileOutputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileName(String fileUrl) {
        return Path.of(fileUrl).getFileName().toString();
    }

    private static boolean notAlreadyLoading(FileMetaData metaData, FileCopyProcess copyProcess) {
        return !(nonNull(copyProcess) && copyProcess.getMetaData().getUrl().equals(metaData.getUrl()) && copyProcess.getMetaData().getHostId().equals(metaData.getHostId()));
    }

    private static <T> T deserializeBody(byte[] body){
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)
        ){
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> byte[] serializeBody(T obj){
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
        ){
            objectOutputStream.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
