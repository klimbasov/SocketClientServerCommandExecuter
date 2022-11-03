package by.bsuir.server.task.impl;

import by.bsuir.instrumental.pool.QueuePool;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.pool.RingPool;
import by.bsuir.instrumental.pool.SearchableRingPool;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketReceiveTaskImpl implements Task {
    private final QueuePool<Packet> packetQueuePool;
    private final SearchableRingPool<String, AbstractNodeIOWrapper> socketIOWrapperQueuePool;
    @Setter
    @Getter
    @Value("${custom.server.timing.receiveIterationsPerTaskExecution}")
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && !socketIOWrapperQueuePool.isEmpty(); counter++) {
            Optional<AbstractNodeIOWrapper> optional = socketIOWrapperQueuePool.getNext();
            if (optional.isPresent()) {
                AbstractNodeIOWrapper wrapper = optional.get();
                wrapper.receive().ifPresent(obj -> {
                    packetQueuePool.offer(obj);
                    logReceive(obj);
                });
                if(!wrapper.isAvailable()){
                    socketIOWrapperQueuePool.remove(wrapper.getHolder().getIdentifier());
                    log.info("socket " + wrapper.getHolder().getIdentifier() + " disconnected");
                    tryCloseSocket(wrapper);
                }
            }
        }
    }

    private static void tryCloseSocket(AbstractNodeIOWrapper wrapper) {
        try {
            wrapper.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void logReceive(Packet obj) {
        log.info("packet received:" + new String(obj.getSourceId()) + " : " + new String(obj.getTargetId()) + ", type " + obj.getType());
    }
}
