package by.bsuir.server.task.impl;

import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketReceiveTaskImpl implements Task {
    private final Pool<Packet> packetPool;
    private final Pool<AbstractNodeIOWrapper> socketIOWrapperPool;
    @Setter
    @Getter
    @Value("${custom.server.timing.receiveIterationsPerTaskExecution}")
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && !socketIOWrapperPool.isEmpty(); counter++) {
            Optional<AbstractNodeIOWrapper> optional = socketIOWrapperPool.poll();
            if (optional.isPresent()) {
                AbstractNodeIOWrapper wrapper = optional.get();
                wrapper.receive().ifPresent(obj -> {
                    packetPool.offer(obj);
                    logReceive(obj);
                });
                if(wrapper.isAvailable()){
                    socketIOWrapperPool.offer(wrapper);
                }else {
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
        log.info("packet received:" + Arrays.toString(obj.getSourceId()) + " : " + Arrays.toString(obj.getTargetId()) + ", type " + obj.getType());
    }
}
