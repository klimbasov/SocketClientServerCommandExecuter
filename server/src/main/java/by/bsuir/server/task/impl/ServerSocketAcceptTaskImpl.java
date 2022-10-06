package by.bsuir.server.task.impl;

import by.bsuir.server.Pool;
import by.bsuir.server.socket.SocketIOWrapper;
import by.bsuir.server.task.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

@Component
@RequiredArgsConstructor
public class ServerSocketAcceptTaskImpl implements Task {
    private final ServerSocket serverSocket;
    private final Pool<SocketIOWrapper> socketIOWrapperPool;
    @Setter
    @Getter
    private long timeToListen;

    @Override
    public void run() {
        for (int counter = 0; counter < timeToListen; counter++) {
            try {
                Socket socket = serverSocket.accept();
                socketIOWrapperPool.offer(new SocketIOWrapper(socket));
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
