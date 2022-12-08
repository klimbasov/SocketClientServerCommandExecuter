package by.bsuir.server.task.impl;

import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.pool.SearchableRingPool;
import by.bsuir.instrumental.ftp.slftp.packet.type.SlftpPacketType;
import by.bsuir.instrumental.pool.impl.PacketQueuePoolImpl;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.util.NodeIdBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketSendTaskImpl implements Task {
    private final PacketQueuePoolImpl packetQueuePool;
    private final SearchableRingPool<String, AbstractNodeIOWrapper> searchableSocketIOWrapperPool;
    @Setter
    @Getter
    @Value("${custom.server.timing.sendIterationsPerTaskExecution}")
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && !packetQueuePool.isEmpty(); counter++) {
            MultiValueMap<AbstractNodeIOWrapper, Packet> abstractNodeIOWrapperPacketMultiValueMap = new LinkedMultiValueMap<>();
            packetQueuePool.pollAll().forEach(packet -> {
                String id = new String(packet.getTargetId());
                Optional<AbstractNodeIOWrapper> wrapperOptional = searchableSocketIOWrapperPool.find(id);
                if (wrapperOptional.isPresent()) {
                    abstractNodeIOWrapperPacketMultiValueMap.add(wrapperOptional.get(), packet);
                } else {
                    handleNotFoundResponse(packet);
                }
            });
            for(Map.Entry<AbstractNodeIOWrapper, List<Packet>> entry : abstractNodeIOWrapperPacketMultiValueMap.entrySet()){
                try {
                    entry.getKey().send(entry.getValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handleNotFoundResponse(Packet packet) {
        PacketType type = PacketType.getInstance(packet.getType());
        Packet resultPacket = switch (type) {
            case FTP_PACKAGE -> handleSlftpRollback(packet);
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

    private Packet handleSlftpRollback(Packet packet) {
        log.error("(slftp)not found host " + new String(packet.getTargetId()));
        return new Packet(
                packet.getBody(),
                NodeIdBuilder.getServerId().getBytes(),
                packet.getSourceId(),
                PacketType.FTP_PACKAGE.typeId,
                SlftpPacketType.NOT_PASSED.typeId);
    }
}
