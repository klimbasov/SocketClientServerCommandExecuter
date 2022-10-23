package by.bsuir.client.task.impl;

import by.bsuir.client.socket.impl.ClientIOWrapper;
import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWrapperPool;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SocketSendTaskImpl implements Task {
    @Setter
    private byte[] CLIENT_ID;
    private final ClientIOWrapper clientIOWrapper;
    private final SocketIOWrapper socketIOWrapper;
    private final Pool<Packet> packetPool;
    @Setter
    @Getter
    @Value("${client.timing.sendIterationsPerTaskExecution}")
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && !packetPool.isEmpty(); counter++) {
            Optional<Packet> optional = packetPool.poll();
            optional.ifPresent(packet -> {
                if(Arrays.equals(this.CLIENT_ID, packet.getTargetId())){
                    clientIOWrapper.send(packet);
                }else {
                    performSendingPackage(packet);
                }
            });
        }
    }

    private void performSendingPackage(Packet packet) {
        try {
            socketIOWrapper.send(packet);
        } catch (RuntimeException e) {
            handleSendingFault(packet);
        }
    }

    void handleSendingFault(Packet packet) {
        Packet responsePacket = new Packet(null, CLIENT_ID, packet.getSourceId(), PacketType.INFORM_PACKAGE.type, PacketFlags.ABORT.getFlagValue());
        packetPool.offer(responsePacket);
    }
}
