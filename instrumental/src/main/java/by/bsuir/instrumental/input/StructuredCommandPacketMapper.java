package by.bsuir.instrumental.input;

import by.bsuir.instrumental.node.token.IdentificationHolder;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class StructuredCommandPacketMapper implements AutoCloseable{
    private final IdentificationHolder identificationHolder;

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final ObjectOutputStream objectOutputStream;

    public StructuredCommandPacketMapper(IdentificationHolder identificationHolder) {
        this.identificationHolder = identificationHolder;
        byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Packet> toPackets(List<StructuredCommand> structuredCommandList) {
        List<Packet> packets = new LinkedList<>();
        for (StructuredCommand command :
                structuredCommandList) {
            Packet packet = new Packet(serialise(command),
                    identificationHolder.getIdentifier(),
                    command.getTargetIdentifier().getBytes(),
                    PacketType.COMMAND_PACKAGE.type,
                    PacketFlags.ACK.flagValue
            );
            packets.add(packet);
        }
        return packets;
    }

    public List<StructuredCommand> toStructuredCommand(@NotNull List<Packet> input) {
        throwIfInvalidIdentifier();
        return input.stream()
                .map(packet ->deserialize(packet.getBody()))
                .toList();
    }

    private void throwIfInvalidIdentifier() {
        if(!identificationHolder.isIdentificationValid()){
            throw new RuntimeException("client has invalid identifier");
        }
    }

    private byte[] serialise(StructuredCommand command) {
        try {
            objectOutputStream.writeObject(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private StructuredCommand deserialize(byte[] serializedCommand){
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedCommand);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (StructuredCommand) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() throws Exception {
        objectOutputStream.close();
        byteArrayOutputStream.close();
    }
}
