package by.bsuir.client.task.impl;

import by.bsuir.client.socket.impl.ClientIOWrapper;
import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWrapperPool;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SocketReceiveTaskImpl implements Task {
    private final Pool<AbstractNodeIOWrapper> nodeIOWrapperPool;
    private final Pool<Packet> packetPool;
    @Setter
    @Getter
    @Value("${client.timing.receiveIterationsPerTaskExecution}")
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall; counter++) {
            nodeIOWrapperPool.poll().flatMap(abstractNodeIOWrapper -> {
                nodeIOWrapperPool.offer(abstractNodeIOWrapper);
                return abstractNodeIOWrapper.receive();
            }).ifPresent(packetPool::offer);
        }
    }
}
