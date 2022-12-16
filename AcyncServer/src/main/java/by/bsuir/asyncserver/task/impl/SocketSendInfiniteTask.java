package by.bsuir.asyncserver.task.impl;

import by.bsuir.instrumental.ftp.slftp.packet.type.SlftpPacketType;
import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWWrapperRingSearchablePool;
import by.bsuir.instrumental.pool.impl.PacketQueuePoolImpl;
import by.bsuir.instrumental.state.application.StateHolder;
import by.bsuir.instrumental.task.InfiniteTask;
import by.bsuir.instrumental.util.NodeIdBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class SocketSendInfiniteTask implements InfiniteTask {
    private final PacketQueuePoolImpl packetQueuePool;
    private final AbstractNodeIOWWrapperRingSearchablePool wrappers;
    private final StateHolder stateHolder;
    private boolean isRunning = true;
    @Setter
    @Getter
    @Value("${custom.server.timing.sendIterationsPerTaskExecution}")
    private int requestsPerCall;

    @Override
    public void run() {
        while (isRunning && stateHolder.isRunning()) {
            try {
                MultiValueMap<AbstractNodeIOWrapper, Packet> abstractNodeIOWrapperPacketMultiValueMap = new LinkedMultiValueMap<>();
                packetQueuePool.pollAll().forEach(packet -> {
                    String id = new String(packet.getTargetId());
                    Optional<AbstractNodeIOWrapper> wrapperOptional = wrappers.find(id);
                    if (wrapperOptional.isPresent()) {
                        abstractNodeIOWrapperPacketMultiValueMap.add(wrapperOptional.get(), packet);
                    } else {
                        handleNotFoundResponse(packet);
                    }
                });
                for (Map.Entry<AbstractNodeIOWrapper, List<Packet>> entry : abstractNodeIOWrapperPacketMultiValueMap.entrySet()) {
                    entry.getKey().send(entry.getValue());
                }
            } catch (RuntimeException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void handleNotFoundResponse(Packet packet) {
        PacketType type = PacketType.getInstance(packet.getType());
        Packet resultPacket = switch (type) {
            case FTP_PACKAGE -> handleFtpRollback(packet);
            default -> handleDefault(packet);
        };
        packetQueuePool.offer(resultPacket);
    }

    private Packet handleDefault(Packet packet) {
        log.error("(def)not found host " + new String(packet.getTargetId()));
        return new Packet(
                "no consumer found".getBytes(),
                NodeIdBuilder.getServerId().getBytes(),
                packet.getSourceId(),
                PacketType.INFORM_PACKAGE.typeId,
                PacketFlags.ABORT.flagValue);

    }

    private Packet handleFtpRollback(Packet packet) {
        log.error("(ftp)not found host " + new String(packet.getTargetId()));
        return new Packet(
                packet.getBody(),
                NodeIdBuilder.getServerId().getBytes(),
                packet.getSourceId(),
                PacketType.FTP_PACKAGE.typeId,
                SlftpPacketType.NOT_PASSED.typeId);
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
