package by.bsuir.client.task.impl;

import by.bsuir.instrumental.node.EndNodeIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.QueuePool;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClientReceiveTaskImpl implements Task {
    private final EndNodeIOWrapper wrapper;
    private final QueuePool<Packet> packetQueuePool;
    @Setter
    @Getter
    @Value("${client.timing.receiveIterationsPerTaskExecution}")
    private int requestsPerCall;

    public ClientReceiveTaskImpl(EndNodeIOWrapper wrapper, @Qualifier("outputPoll") QueuePool<Packet> packetQueuePool) {
        this.wrapper = wrapper;
        this.packetQueuePool = packetQueuePool;
    }

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall; counter++) {
            wrapper.receive().ifPresent(packetQueuePool::offer);
        }
    }
}
