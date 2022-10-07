package by.bsuir.server.task.impl;

import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SocketReceiveTaskImpl implements Task {
    private final Pool<Packet> packetPool;
    private final Pool<AbstractNodeIOWrapper> socketIOWrapperPool;
    @Setter
    @Getter
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall; counter++) {
            Optional<AbstractNodeIOWrapper> optional = socketIOWrapperPool.poll();
            if (optional.isPresent()) {
                AbstractNodeIOWrapper abstractNodeIOWrapper = optional.get();
                abstractNodeIOWrapper.receive().ifPresent(packetPool::offer);
                socketIOWrapperPool.offer(abstractNodeIOWrapper);
            } else {
                break;
            }
        }
    }
}
