package by.bsuir.instrumental.node;

import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.packet.Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractNodeIOWrapper implements Comparable<AbstractNodeIOWrapper>, AutoCloseable {
    @Getter
    private final IdentificationHolder holder;

    public abstract Optional<Packet> receive();

    public abstract void send(Packet response) throws IOException;

    public abstract boolean isAvailable();

    @Override
    public int compareTo(AbstractNodeIOWrapper o) {
        if (o == null) {
            return 1;
        }
        return this.holder.compareTo(o.getHolder());
    }
}
