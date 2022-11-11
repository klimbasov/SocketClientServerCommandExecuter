package by.bsuir.client.task.impl;

import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.QueuePool;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SocketSendTaskImpl implements Task {
    private final SocketIOWrapper socketIOWrapper;
    private final QueuePool<Packet> packetQueuePool;
    @Setter
    @Getter
    @Value("${client.timing.sendIterationsPerTaskExecution}")
    private int requestsPerCall;

    public SocketSendTaskImpl(SocketIOWrapper socketIOWrapper, @Qualifier("outputPoll") QueuePool<Packet> packetQueuePool) {
        this.socketIOWrapper = socketIOWrapper;
        this.packetQueuePool = packetQueuePool;
    }

    @Override
    public void run() {
        if (socketIOWrapper.isAvailable()) {
            for (int counter = 0; counter < requestsPerCall && !packetQueuePool.isEmpty(); counter++) {
                Optional<Packet> optional = packetQueuePool.poll();
                optional.ifPresent(socketIOWrapper::send);
            }
        }
    }
}
