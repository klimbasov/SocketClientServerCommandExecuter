package by.bsuir.server.packet;

import by.bsuir.server.Pool;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Component
public class PacketPoolImpl implements Pool<Packet> {
    private final Queue<Packet> packetQueue = new LinkedList<>();

    @Override
    public void offer(Packet packet) {
        packetQueue.offer(packet);
    }

    @Override
    public Optional<Packet> poll() {
        return Optional.ofNullable(packetQueue.poll());
    }

    @Override
    public boolean isEmpty() {
        return packetQueue.isEmpty();
    }
}
