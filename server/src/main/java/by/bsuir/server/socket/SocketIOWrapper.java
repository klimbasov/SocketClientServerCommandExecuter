package by.bsuir.server.socket;

import by.bsuir.server.packet.Packet;
import lombok.Getter;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

import static java.util.Objects.isNull;

public class SocketIOWrapper implements AutoCloseable, Comparable<SocketIOWrapper> {
    private static final int DEFAULT_SO_TIMEOUT = 50;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    @Getter
    private final String socketId;
    @Getter
    private boolean isClosed = false;

    public SocketIOWrapper(Socket socket) {
        try {
            socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
            this.socket = socket;
            this.socketId = buildSocketId(socket);
            in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildSocketId(Socket socket) {
        return socket.getRemoteSocketAddress() + ":" + socket.getPort() + "|" + socket.getLocalSocketAddress() + ":" + socket.getLocalPort();
    }

    public Optional<Packet> receive() {
        Packet request;
        try {
            request = (Packet) in.readObject();
            if (isNull(request)) {
                isClosed = true;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(request);
    }

    public void send(Packet response) throws IOException {
        out.writeObject(response);
    }

    @Override
    public void close() throws Exception {
        in.close();
        out.close();
        socket.close();
    }

    @Override
    public int compareTo(SocketIOWrapper o) {
        return this.socketId.compareTo(o.socketId);
    }
}
