package by.bsuir.instrumental.node;

import by.bsuir.instrumental.packet.Packet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.*;
import java.util.Optional;

public abstract class AbstractNodeIOWrapper implements Comparable<AbstractNodeIOWrapper> {
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String socketId;

    public abstract Optional<Packet> receive();

    public abstract void send(Packet response) throws IOException;

    @Override
    public int compareTo(AbstractNodeIOWrapper o) {
        return this.socketId.compareTo(o.socketId);
    }
}
