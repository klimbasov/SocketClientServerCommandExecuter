package by.bsuir.server.task.impl;

import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.pool.SearchablePool;
import by.bsuir.instrumental.slftp.packet.type.SlftpPacketType;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.util.NodeIdBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketSendTaskImpl implements Task {
    private final Pool<Packet> packetPool;
    private final SearchablePool<String, AbstractNodeIOWrapper> searchableSocketIOWrapperPool;
    @Setter
    @Getter
    @Value("${custom.server.timing.sendIterationsPerTaskExecution}")
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && !packetPool.isEmpty(); counter++) {
            Optional<Packet> optional = packetPool.poll();
            optional.ifPresent(packet -> {
                String id = new String(packet.getTargetId());
                Optional<AbstractNodeIOWrapper> wrapperOptional = searchableSocketIOWrapperPool.find(id);
                if(wrapperOptional.isPresent()){
                    handleSendingToNode(packet, id, wrapperOptional.get());
                }else {
                    handleNotFoundResponse(packet);
                }
            });
        }
    }

    private void handleNotFoundResponse(Packet packet) {
        PacketType type = PacketType.getInstance(packet.getType());
        Packet resultPacket = switch (type){
            case SLFTP_PACKAGE -> handleSlftpRollback(packet);
            default -> handleDefault(packet);
        };
        packetPool.offer(resultPacket);
    }

    private Packet handleDefault(Packet packet) {
        log.error("(def)not found host " + packet.getTargetId());
        return new Packet(
                "no consumer found".getBytes(),
                NodeIdBuilder.getServerId().getBytes(),
                packet.getSourceId(),
                PacketType.INFORM_PACKAGE.typeId,
                PacketFlags.ABORT.flagValue);

    }

    private Packet handleSlftpRollback(Packet packet) {
        log.error("(slftp)not found host " + packet.getTargetId());
        return new Packet(
                packet.getBody(),
                NodeIdBuilder.getServerId().getBytes(),
                packet.getSourceId(),
                PacketType.SLFTP_PACKAGE.typeId,
                SlftpPacketType.NOT_PASSED.typeId);
    }

    private void handleSendingToNode(Packet packet, String id, AbstractNodeIOWrapper socketIOWrapper) {
            if (socketIOWrapper.isAvailable()) {
                try {
                    socketIOWrapper.send(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Optional<AbstractNodeIOWrapper> wrapper = searchableSocketIOWrapperPool.remove(id);
                if (wrapper.isPresent()) {
                    try {
                        wrapper.get().close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
}
