package by.bsuir.server.task.impl;

import by.bsuir.server.Pool;
import by.bsuir.server.packet.Packet;
import by.bsuir.server.socket.SocketIOWrapper;
import by.bsuir.server.task.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SocketReceiveTaskImpl implements Task {
    private final Pool<Packet> packetPool;
    private final Pool<SocketIOWrapper> socketIOWrapperPool;
    @Setter
    @Getter
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall; counter++) {
            Optional<SocketIOWrapper> optional = socketIOWrapperPool.poll();
            if (optional.isPresent()) {
                SocketIOWrapper socketIOWrapper = optional.get();
                socketIOWrapper.receive().ifPresent(packetPool::offer);
                socketIOWrapperPool.offer(socketIOWrapper);
            } else {
                break;
            }
        }
    }
}
