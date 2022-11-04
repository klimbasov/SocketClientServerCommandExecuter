package by.bsuir.instrumental.node;

import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.packet.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;

import static java.util.Objects.isNull;

@Slf4j
public class SocketIOWrapper extends AbstractNodeIOWrapper implements DisposableBean {
    @Getter
    private Socket socket;
    private static final int DEFAULT_SO_TIMEOUT = 50;
    @Getter
    private boolean isClosed = false;

    private ObjectOutputStream objectOutputStream;
    private ByteArrayOutputStream byteArrayOutputStream;

    private Queue<Packet> packetQueue;

    public SocketIOWrapper(Socket socket, IdentificationHolder holder) {
        super(holder);
        packetQueue = new LinkedList<>();
        byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            this.socket = socket;
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//            socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSocket(Socket socket) throws IOException {
        this.socket = socket;
        byteArrayOutputStream = new ByteArrayOutputStream();
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//        socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
        isClosed = socket.isClosed();
    }

    public SocketIOWrapper(IdentificationHolder holder){
        super(holder);
        isClosed = true;
    }

    public Optional<Packet> receive() {
        List<Packet> packets = new ArrayList<>(30);

        try {
            InputStream inputStream = socket.getInputStream();
            byte[] data = inputStream.readNBytes(inputStream.available());
            byteArrayOutputStream.write(data);
            try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)){
                try {
                    while(true){
                        packets.add((Packet) objectInputStream.readObject());
                    }
                }catch (EOFException e){}
                byteArrayInputStream.transferTo(byteArrayOutputStream);
            }

//            if (isNull(request)) {
//                isClosed = true;
//            }
        }catch (SocketTimeoutException ignored){}
        catch (EOFException e){
            isClosed = true;
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

//    private Packet redirectToHost(Packet request) {
//        if(Arrays.equals(request.getTargetId(), super.getHolder().getIdentifier().getBytes())){
//            request = new Packet(request.getBody(), request.getSourceId(), super.getHolder().getIdentifier().getBytes(), request.getType(), request.getFlags());
//        }
//        return request;
//    }

    public void send(Packet response) {
        try {
//            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(response);
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
                objectOutputStream.close();
                byteArrayOutputStream.close();
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void close() {
        if(socket != null){
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            boolean wasClosed = socket.isClosed();
            try {
                socket.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            if(!wasClosed && socket.isClosed()){
                log.info("wrapper " + getHolder().getIdentifier() + " was closed.");
            }
        }
    }
}
