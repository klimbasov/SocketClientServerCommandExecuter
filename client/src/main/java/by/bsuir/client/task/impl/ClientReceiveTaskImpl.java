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

@Component
public class ClientReceiveTaskImpl implements Task {
    private final ClientIOWrapper wrapper;
    private final Pool<Packet> packetPool;
    @Setter
    @Getter
    @Value("${client.timing.receiveIterationsPerTaskExecution}")
    private int requestsPerCall;

    public ClientReceiveTaskImpl(ClientIOWrapper wrapper, @Qualifier("outputPoll") Pool<Packet> packetPool) {
        this.wrapper = wrapper;
        this.packetPool = packetPool;
    }

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall; counter++) {
            wrapper.receive().ifPresent(packetPool::offer);
        }
    }
}
