package by.bsuir.server.task.impl;

import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.QueuePool;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWWrapperRingSearchablePool;
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
    private final AbstractNodeIOWWrapperRingSearchablePool wrappers;
    @Setter
    @Getter
    @Value("${custom.server.timing.receiveIterationsPerTaskExecution}")
    private int requestsPerCall;

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

    @Override
    public void run() {
        for (int counter = 0; counter < wrappers.size(); counter++) {
            Optional<AbstractNodeIOWrapper> optional = wrappers.getNext();
            if (optional.isPresent()) {
                AbstractNodeIOWrapper wrapper = optional.get();
                wrapper.receive().forEach(obj -> {
                    String uuid = new String(obj.getSourceId());
                    wrappers.setName(uuid, wrapper);
                    packetQueuePool.offer(obj);
                    logReceive(obj);
                });
                if (!wrapper.isAvailable()) {
                    wrappers.remove(wrapper);
                    log.info("socket " + wrapper.getHolder().getIdentifier() + " disconnected");
                    tryCloseSocket(wrapper);
                }
            }
        }
    }
}
