package by.bsuir.instrumental.ftp.slftp;

import by.bsuir.instrumental.ftp.FtpController;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.pool.SearchableQueuePool;
import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.slftp.dto.Portion;
import by.bsuir.instrumental.ftp.slftp.dto.PortionRequest;
import by.bsuir.instrumental.ftp.slftp.meta.FileCopyProcess;
import by.bsuir.instrumental.ftp.slftp.meta.InputFileRecord;
import by.bsuir.instrumental.ftp.slftp.packet.type.SlftpPacketType;
import by.bsuir.instrumental.ftp.slftp.pool.FileProcessUriQueuePool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Slf4j
public class SlftpController implements FtpController {
    private static final short MAX_DECLINED_TIME = 3;
    private static final int PORTION_SIZE = 1024 << 7;
    private static final int MAX_IDLE_TIME = 512;
    private static int idleTime = 0;
    private final IdentificationHolder holder;
    private final FileProcessUriQueuePool processSearchablePool;
    private final SearchableQueuePool<String, InputFileRecord> inputFileRecordSearchablePool;
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
            throw new RuntimeException("slftp controller got non slftp packet");
        }
        switch (SlftpPacketType.getByTypeId(packet.getFlags())) {
            case GREETING -> handleGreeting(packet);
            case PORTION -> handlePortion(packet);
            case PORTION_REQ -> handlePortionReq(packet);
            case ABORT -> handleAbort(packet);
            case DECLINE -> handleDecline(packet);
            case NOT_PASSED -> handleNotPassed(packet);
            case GREETING_REQ -> handleGreetingReq(packet);
            default -> log.error("nothing great happened");
        }
    }

    @Override
    public void upload(String uri, String destinationId) {
        Optional<FileMetaData> optional = getMetadataByFileUrl(uri);
        optional.ifPresent(fileMetaData -> sendPacket(serializeBody(fileMetaData), holder.getIdentifier().getBytes(), destinationId.getBytes(), SlftpPacketType.GREETING));
    }

    @Override
    public void download(String uri, String destinationId) {
        Optional<FileMetaData> optional = Optional.of(new FileMetaData().setHostId(destinationId).setUrl(uri));
        optional.ifPresent(fileMetaData -> sendPacket(serializeBody(fileMetaData), holder.getIdentifier().getBytes(), destinationId.getBytes(), SlftpPacketType.GREETING_REQ));
    }

    private static void addProcessMils(FileCopyProcess copyProcess) {
        copyProcess.setMils(copyProcess.getMils() + (System.currentTimeMillis() - copyProcess.getLastTimeTransceive()));
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

    private static long getPortionsQuantity(FileMetaData metaData) {
        return (metaData.getSize() + (PORTION_SIZE - 1)) / PORTION_SIZE;
    }

    private static boolean notAlreadyLoading(FileMetaData metaData, FileCopyProcess copyProcess) {
        return !(nonNull(copyProcess) && copyProcess.getMetaData().getUrl().equals(metaData.getUrl()) && copyProcess.getMetaData().getHostId().equals(metaData.getHostId()));
    }

    private static <T> T deserializeBody(byte[] body) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)
        ) {
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> byte[] serializeBody(T obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
        ) {
            objectOutputStream.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleGreetingReq(Packet packet){
        FileMetaData metaData = deserializeBody(packet.getBody());
        upload(metaData.getUrl(), metaData.getHostId());
    }

    private void handleNotPassed(Packet packet) {
        PortionRequest request = deserializeBody(packet.getBody());
        sendPacket(packet.getBody(), holder.getIdentifier().getBytes(), request.getHostId().getBytes(), SlftpPacketType.PORTION_REQ);
    }

    private void restoreExistingProcess() {
        Optional<FileCopyProcess> optional = processSearchablePool.poll();
        if (optional.isPresent()) {
            FileCopyProcess copyProcess = optional.get();
            copyProcess.setLastTimeTransceive(System.currentTimeMillis());
            PortionRequest request = getPortionRequestFromCopy(copyProcess);
            sendPacket(serializeBody(request), holder.getIdentifier().getBytes(), copyProcess.getMetaData().getHostId().getBytes(), SlftpPacketType.PORTION_REQ);
            processSearchablePool.offer(copyProcess);
            idleTime = 0;
        }
    }

    private void handleDecline(Packet packet) {
        PortionRequest request = deserializeBody(packet.getBody());
        Optional<FileCopyProcess> optional = processSearchablePool.find(request.getFileUri());
        if (optional.isPresent()) {
            FileCopyProcess copyProcess = optional.get();
            copyProcess.setTimesDeclined((short) (copyProcess.getTimesDeclined() + 1));
            if (copyProcess.getTimesDeclined() == MAX_DECLINED_TIME) {
                FileMetaData metaData = copyProcess.getMetaData();
                processSearchablePool.remove(metaData.getUrl());

                log.info("file " + metaData.getUrl() + " from host " + metaData.getHostId() + " was not loaded");
            } else {
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
            if (record.isEmpty()) {
                record = tryCreateRecord(request);
            }
            if (record.isPresent()) {
                Portion portion = readPortion(record.get(), request);
                sendPacket(serializeBody(portion), packet.getTargetId(), packet.getSourceId(), SlftpPacketType.PORTION);
            } else {
                sendPacket(packet.getBody(), packet.getTargetId(), packet.getSourceId(), SlftpPacketType.DECLINE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //стз
    private void handlePortion(Packet packet) {
        Portion portion = deserializeBody(packet.getBody());
        Optional<FileCopyProcess> optional = processSearchablePool.find(portion.getFileUri());
        if (optional.isPresent()) {
            FileCopyProcess copyProcess = optional.get();
            addProcessMils(copyProcess);
            if (copyProcess.getPortion() == portion.getPortionNum()) {
                writePortionToFile(copyProcess, portion);
            }
            if (copyProcess.getPortion() == copyProcess.getPortionsQuantity()) {
                log.info("transferring " + copyProcess.getMetaData().getUrl() + " finished. Bitrate " + ((double) copyProcess.getMetaData().getSize()) / copyProcess.getMils() * 8000 + " bits/sec");
                sendPacket(serializeBody(copyProcess.getMetaData()), packet.getTargetId(), packet.getSourceId(), SlftpPacketType.ABORT);
                processSearchablePool.remove(copyProcess.getMetaData().getUrl());
                closeClosable(copyProcess);
            } else {
                PortionRequest request = getPortionRequestFromCopy(copyProcess);
                sendPacket(serializeBody(request), holder.getIdentifier().getBytes(), copyProcess.getMetaData().getHostId().getBytes(), SlftpPacketType.PORTION_REQ);
                copyProcess.setLastTimeTransceive(System.currentTimeMillis());
            }
        }
    }

    private void handleGreeting(Packet packet) {
        FileMetaData metaData = deserializeBody(packet.getBody());
        long portionsQuantity = getPortionsQuantity(metaData);
        FileCopyProcess copyProcess = processSearchablePool.find(metaData.getUrl()).orElse(null);

        if (notAlreadyLoading(metaData, copyProcess)) {
            FileOutputStream outputStream = openOutputStream(metaData);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            copyProcess = new FileCopyProcess()
                    .setMils(0)
                    .setMetaData(metaData)
                    .setPortion(0)
                    .setPortionsQuantity(portionsQuantity)
                    .setLastTimeTransceive(System.currentTimeMillis())
                    .setStream(bufferedOutputStream);
            processSearchablePool.offer(copyProcess);
            PortionRequest request = getPortionRequestFromCopy(copyProcess);
            sendPacket(serializeBody(request), holder.getIdentifier().getBytes(), copyProcess.getMetaData().getHostId().getBytes(), SlftpPacketType.PORTION_REQ);
        }
    }

    private void sendPacket(byte[] packet, byte[] sourceId, byte[] targetId, SlftpPacketType decline) {
        Packet resultPacket = new Packet(packet, sourceId, targetId, PacketType.FTP_PACKAGE.typeId, decline.typeId);
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

    private Optional<FileMetaData> getMetadataByFileUrl(String url) {
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

    private String getFileName(String fileUrl) {
        return Path.of(fileUrl).getFileName().toString();
    }

    private void createDownloadDirIfNotExist(){
        File directory = new File(DOWNLOAD_DIRECTORY_PATH);
        if(!directory.exists()||!directory.isDirectory()){
            directory.mkdirs();
        }

    }

    @Override
    public void close() {

    }
}
