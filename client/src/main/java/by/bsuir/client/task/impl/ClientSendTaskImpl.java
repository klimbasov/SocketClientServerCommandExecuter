package by.bsuir.client.task.impl;

import by.bsuir.client.socket.impl.ClientIOWrapper;
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
public class ClientSendTaskImpl implements Task {
    private final ClientIOWrapper wrapper;
    private final QueuePool<Packet> packetQueuePool;
    @Setter
    @Getter
    @Value("${client.timing.sendIterationsPerTaskExecution}")
    private int requestsPerCall;

    public ClientSendTaskImpl(ClientIOWrapper wrapper, @Qualifier("inputQueuePool") QueuePool<Packet> packetQueuePool) {
        this.wrapper = wrapper;
        this.packetQueuePool = packetQueuePool;
    }

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && !packetQueuePool.isEmpty(); counter++) {
            Optional<Packet> optional = packetQueuePool.poll();
            optional.ifPresent(wrapper::send);
        }
    }
}
