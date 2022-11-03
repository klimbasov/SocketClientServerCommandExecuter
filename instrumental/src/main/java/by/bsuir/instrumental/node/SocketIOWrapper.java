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
import java.util.Optional;

import static java.util.Objects.isNull;

@Slf4j
public class SocketIOWrapper extends AbstractNodeIOWrapper implements DisposableBean {
    @Getter
    private Socket socket;
    private static final int DEFAULT_SO_TIMEOUT = 50;
    @Getter
    private boolean isClosed = false;

    public SocketIOWrapper(Socket socket, IdentificationHolder holder) {
        super(holder);
        try {
            this.socket = socket;
            socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSocket(Socket socket) throws SocketException {
        this.socket = socket;
        socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
        isClosed = socket.isClosed();
    }

    public SocketIOWrapper(IdentificationHolder holder){
        super(holder);
        isClosed = true;
    }

    public Optional<Packet> receive() {
        Packet request = null;
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            request = (Packet) inputStream.readObject();
            if (isNull(request)) {
                isClosed = true;
            }
        }catch (SocketTimeoutException ignored){}
        catch (EOFException e){
            isClosed = true;
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(request);
    }

//    private Packet redirectToHost(Packet request) {
//        if(Arrays.equals(request.getTargetId(), super.getHolder().getIdentifier().getBytes())){
//            request = new Packet(request.getBody(), request.getSourceId(), super.getHolder().getIdentifier().getBytes(), request.getType(), request.getFlags());
//        }
//        return request;
//    }

    public void send(Packet response) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(response);
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
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void close() throws Exception {
        if(socket != null){
            boolean wasClosed = socket.isClosed();
            socket.close();
            if(!wasClosed && socket.isClosed()){
                log.info("wrapper " + getHolder().getIdentifier() + " was closed.");
            }
        }
    }
}
