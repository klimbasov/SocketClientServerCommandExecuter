package by.bsuir.server.task.impl;

import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.node.identification.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWWrapperRingSearchablePool;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.util.NodeIdBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServerSocketAcceptTaskImpl implements Task {
    private final ServerSocket serverSocket;
    private final AbstractNodeIOWWrapperRingSearchablePool socketIOWrapperQueuePool;
    @Setter
    @Getter
    @Value("${custom.server.timing.socketAcceptIterationsPerTaskExecution}")
    private long timeToListen;

    @Override
    public void run() {
        for (int counter = 0; counter < timeToListen; counter++) {
            try {
                Socket socket = serverSocket.accept();
                IdentificationHolderImpl holder = new IdentificationHolderImpl();
                holder.setId(NodeIdBuilder.buildSocketIdServer(socket));
                SocketIOWrapper wrapper = new SocketIOWrapper(socket, holder);
                socketIOWrapperQueuePool.offerUnnamed(wrapper);
                log.info("socket connection established: " + wrapper.getHolder().getIdentifier());
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
