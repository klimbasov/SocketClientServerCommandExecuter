package by.bsuir.asyncserver.task.impl;

import by.bsuir.asyncserver.pool.MultithreadingSocketHandlerTaskPool;
import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.node.identification.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.pool.impl.PacketQueuePoolImpl;
import by.bsuir.instrumental.state.application.StateHolder;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.util.NodeIdBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

@RequiredArgsConstructor
@Slf4j
public class ServerSocketAcceptTask implements Task, Closeable {
    private final ServerSocket serverSocket;
    private final MultithreadingSocketHandlerTaskPool threadTaskQueuePool;
    private final PacketQueuePoolImpl packetQueuePool;
    private final StateHolder stateHolder;
    @Setter
    @Getter
    private long timeToListen = 1;

    @Override
    public void run() {
        for (int counter = 0; counter < timeToListen; counter++) {
            try {
                Socket socket = serverSocket.accept();
                IdentificationHolderImpl holder = new IdentificationHolderImpl();
                holder.setId(NodeIdBuilder.buildSocketIdServer(socket));
                SocketIOWrapper wrapper = new SocketIOWrapper(socket, holder);
                MultithreadingSocketHandlerTask task = new MultithreadingSocketHandlerTask(wrapper, stateHolder, packetQueuePool);
                if (!threadTaskQueuePool.offer(task)) {
                    socket.close();
                    log.warn("socket cannot be processed due to the server is out of free slots");
                }
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() {
        stateHolder.setRunning(false);
        threadTaskQueuePool.close();
    }
}
