package by.bsuir.client.task.impl;

import by.bsuir.instrumental.node.EndNodeIOWrapper;
import by.bsuir.instrumental.pool.impl.PacketQueuePoolImpl;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClientSendTaskImpl implements Task {
    private final EndNodeIOWrapper wrapper;
    private final PacketQueuePoolImpl packetQueuePool;
    @Setter
    @Getter
    @Value("${client.timing.sendIterationsPerTaskExecution}")
    private int requestsPerCall;

    public ClientSendTaskImpl(EndNodeIOWrapper wrapper, @Qualifier("inputQueuePool") PacketQueuePoolImpl packetQueuePool) {
        this.wrapper = wrapper;
        this.packetQueuePool = packetQueuePool;
    }

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && !packetQueuePool.isEmpty(); counter++) {
            wrapper.send(packetQueuePool.pollAll());
        }
    }
}
