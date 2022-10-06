package by.bsuir.server.task.impl;

import by.bsuir.server.Pool;
import by.bsuir.server.packet.Packet;
import by.bsuir.server.packet.PacketFlags;
import by.bsuir.server.packet.type.PacketType;
import by.bsuir.server.socket.SearchableSocketIOWrapperPool;
import by.bsuir.server.socket.SocketIOWrapper;
import by.bsuir.server.task.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SocketSendTaskImpl implements Task {
    private static final byte[] SERVER_ID = "0.0.0.0:0::0.0.0.0:0".getBytes();  //todo remove this by any means
    private final Pool<Packet> packetPool;
    private final SearchableSocketIOWrapperPool searchableSocketIOWrapperPool;
    @Setter
    @Getter
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && !packetPool.isEmpty(); counter++) {
            Optional<Packet> optional = packetPool.poll();
            optional.ifPresent(packet -> {
                String id = new String(packet.getTargetId());
                searchableSocketIOWrapperPool.find(id).ifPresent(
                        socketIOWrapper -> performSendingPackage(packet, socketIOWrapper)
                );
            });
        }
    }

    private void performSendingPackage(Packet packet, SocketIOWrapper socketIOWrapper) {
        try {
            socketIOWrapper.send(packet);
        } catch (IOException e) {
            handleSendingFault(packet);
        }
    }

    void handleSendingFault(Packet packet) {
        Packet responsePacket = new Packet(null, SERVER_ID, packet.getSourceId(), PacketType.INFORM_PACKAGE.type, PacketFlags.ABORT.getFlagValue());
        packetPool.offer(responsePacket);
    }
}
