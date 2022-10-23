package by.bsuir.instrumental.node;

import by.bsuir.instrumental.packet.Packet;
import lombok.Getter;
import org.springframework.beans.factory.DisposableBean;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

import static java.util.Objects.isNull;

public class SocketIOWrapper extends AbstractNodeIOWrapper implements DisposableBean {
    private static final int DEFAULT_SO_TIMEOUT = 50;
    private final Socket socket;
    @Getter
    private boolean isClosed = false;

    public SocketIOWrapper(Socket socket) {
        super.setSocketId(buildSocketId(socket));
        try {
            this.socket = socket;
            socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildSocketId(Socket socket) {
        return socket.getRemoteSocketAddress() + ":" + socket.getPort() + "|" + socket.getLocalSocketAddress() + ":" + socket.getLocalPort();
    }

    public Optional<Packet> receive() {
        Packet request;
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()))){
            request = (Packet) in.readObject();
            if (isNull(request)) {
                isClosed = true;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(request);
    }

    public void send(Packet response) {
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()))){
            out.writeObject(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        socket.close();
    }
}
