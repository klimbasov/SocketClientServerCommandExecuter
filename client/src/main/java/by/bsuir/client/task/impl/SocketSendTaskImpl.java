package by.bsuir.client.task.impl;

import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SocketSendTaskImpl implements Task {
    private final SocketIOWrapper socketIOWrapper;
    private final Pool<Packet> packetPool;
    @Setter
    @Getter
    @Value("${client.timing.sendIterationsPerTaskExecution}")
    private int requestsPerCall;

    public SocketSendTaskImpl(SocketIOWrapper socketIOWrapper,@Qualifier("outputPoll") Pool<Packet> packetPool) {
        this.socketIOWrapper = socketIOWrapper;
        this.packetPool = packetPool;
    }

    @Override
    public void run() {
        if(socketIOWrapper.isAvailable()){
            for (int counter = 0; counter < requestsPerCall && !packetPool.isEmpty(); counter++) {
                Optional<Packet> optional = packetPool.poll();
                optional.ifPresent(socketIOWrapper::send);
            }
        }
    }
//    private void performSendingPackage(Packet packet) {
//        try {
//            socketIOWrapper.send(packet);
//        } catch (RuntimeException e) {
//            handleSendingFault(packet);
//        }
//    }
//
//    void handleSendingFault(Packet packet) {
//        Packet responsePacket = new Packet(null, CLIENT_ID, packet.getSourceId(), PacketType.INFORM_PACKAGE.type, PacketFlags.ABORT.getFlagValue());
//        packetPool.offer(responsePacket);
//    }
}
