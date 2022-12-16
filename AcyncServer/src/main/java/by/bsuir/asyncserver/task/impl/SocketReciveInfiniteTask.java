package by.bsuir.asyncserver.task.impl;

import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.QueuePool;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWWrapperRingSearchablePool;
import by.bsuir.instrumental.state.application.StateHolder;
import by.bsuir.instrumental.task.InfiniteTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class SocketReciveInfiniteTask implements InfiniteTask {
    private boolean isRunning = true;

    private final QueuePool<Packet> packetQueuePool;
    private final AbstractNodeIOWWrapperRingSearchablePool wrappers;
    private final StateHolder stateHolder;
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
        while (isRunning && stateHolder.isRunning()){
            try {
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
            }catch (RuntimeException e){
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
