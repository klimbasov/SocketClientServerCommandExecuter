package by.bsuir.instrumental.node;

import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.packet.Packet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;

@Slf4j
public class SocketIOWrapper extends AbstractNodeIOWrapper implements DisposableBean {
    @Getter
    private Socket socket;

    private static final byte[] OBJECT_STREAM_HEADER = new byte[] {-84, -19, 0, 5};
    private static final int OBJECT_STREAM_HEADER_LENGTH = 4;
    private static final int DEFAULT_SO_TIMEOUT = 50;
    @Getter
    private boolean isClosed = false;

    private ByteArrayOutputStream holderStream;

    private Queue<Packet> packetQueue;

    public SocketIOWrapper(Socket socket, IdentificationHolder holder) {
        super(holder);
        initNonfaultState();
        try {
            setAndConfigureSocket(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SocketIOWrapper(IdentificationHolder holder){
        super(holder);
        isClosed = true;
    }

    private void setAndConfigureSocket(Socket socket) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
        flushSocketInputStream(socket);
    }

    private void flushSocketInputStream(Socket socket) throws IOException {
        InputStream stream = socket.getInputStream();
        int len = stream.available();
        if (len != 0 ){
            stream.readNBytes(len);
        }
    }

    private void initNonfaultState() {
        packetQueue = new LinkedList<>();
        holderStream = new ByteArrayOutputStream();
    }

    public void setSocket(Socket socket) throws IOException {
        setAndConfigureSocket(socket);
        initNonfaultState();
        isClosed = socket.isClosed();
    }

    public Optional<Packet> receive() {
        if(packetQueue.isEmpty()){
            handleRead();
        }
        return Optional.ofNullable(packetQueue.poll());
    }

    private void handleRead() {
        List<Packet> packets = new ArrayList<>(30){};

        try {
            InputStream inputStream = socket.getInputStream();
            int availableBytes = inputStream.available();
            if(availableBytes > 0){
                byte[] data = inputStream.readNBytes(availableBytes);
                packets = dataHandler(data);
            }else {
                whaitSocketResponceTillTimeout(inputStream);
            }
        }
        catch (SocketException e){
            this._close();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        packetQueue.addAll(packets);
    }

    private void whaitSocketResponceTillTimeout(InputStream inputStream) throws IOException {
        try {
            int checkByte = inputStream.read();
            if(checkByte==-1){
                this.close();
            }else {
                holderStream.write(checkByte);
            }
        }catch (SocketTimeoutException ignored){}
    }

    private List<Packet> dataHandler(byte[] data) throws IOException, ClassNotFoundException {
        List<Packet> packets = new ArrayList<>(30);
        holderStream.write(data);
        byte[] bufferedData = holderStream.toByteArray();
        int lastReadPos = bufferedData.length;
        long skippedBytes = 0;
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bufferedData);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream)){
            try {
                while(true){
                    packets.add((Packet) objectInputStream.readObject());
                    skippedBytes = bufferedInputStream.skip(OBJECT_STREAM_HEADER_LENGTH);
                    lastReadPos = bufferedInputStream.available();
                }
            }catch (EOFException e){
            }catch (StreamCorruptedException e){
                log.error(e.getMessage());
            }
            finally {
                holderStream.reset();
                if(skippedBytes != 0 ){
                    holderStream.write(OBJECT_STREAM_HEADER, 0, (int)skippedBytes);
                }
                if(lastReadPos != 0){
                    holderStream.write(bufferedData, bufferedData.length - lastReadPos, lastReadPos);
                }
            }

        }
        return packets;
    }

//    private Packet redirectToHost(Packet request) {
//        if(Arrays.equals(request.getTargetId(), super.getHolder().getIdentifier().getBytes())){
//            request = new Packet(request.getBody(), request.getSourceId(), super.getHolder().getIdentifier().getBytes(), request.getType(), request.getFlags());
//        }
//        return request;
//    }

    public void send(Packet response) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(response);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            socket.getOutputStream().write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAvailable() {
        return !isClosed;
    }

    @Override
    public void destroy(){
        try {
            if(socket != null){
                holderStream.close();
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void _close(){
        if(socket != null){
            boolean wasClosed = socket.isClosed();
            try {
                socket.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            isClosed = true;
            if(!wasClosed && socket.isClosed()){
                log.info("wrapper " + getHolder().getIdentifier() + " was closed.");
            }
        }
    }

    @Override
    public void close() {
        if(holderStream != null){
            try {
                holderStream.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        _close();
    }
}
