package by.bsuir.client.task.impl;

import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.pool.impl.PacketQueuePoolImpl;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SocketSendTaskImpl implements Task {
    private final SocketIOWrapper socketIOWrapper;
    private final PacketQueuePoolImpl packetQueuePool;
    @Setter
    @Getter
    @Value("${client.timing.sendIterationsPerTaskExecution}")
    private int requestsPerCall;

    public SocketSendTaskImpl(SocketIOWrapper socketIOWrapper, @Qualifier("outputPoll") PacketQueuePoolImpl packetQueuePool) {
        this.socketIOWrapper = socketIOWrapper;
        this.packetQueuePool = packetQueuePool;
    }

    @Override
    public void run() {
        if (socketIOWrapper.isAvailable()) {
            for (int counter = 0; counter < requestsPerCall && !packetQueuePool.isEmpty(); counter++) {
                socketIOWrapper.send(packetQueuePool.pollAll());
            }
        }
    }
}
