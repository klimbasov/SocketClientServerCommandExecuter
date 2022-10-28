package by.bsuir.client.task.impl;

import by.bsuir.client.socket.impl.ClientIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.Pool;
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
    private final Pool<Packet> packetPool;
    @Setter
    @Getter
    @Value("${client.timing.sendIterationsPerTaskExecution}")
    private int requestsPerCall;

    public ClientSendTaskImpl(ClientIOWrapper wrapper, @Qualifier("inputPool") Pool<Packet> packetPool) {
        this.wrapper = wrapper;
        this.packetPool = packetPool;
    }

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && !packetPool.isEmpty(); counter++) {
            Optional<Packet> optional = packetPool.poll();
            optional.ifPresent(wrapper::send);
        }
    }
}
