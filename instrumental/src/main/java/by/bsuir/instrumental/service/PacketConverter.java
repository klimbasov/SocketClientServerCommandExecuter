package by.bsuir.instrumental.service;

import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class PacketConverter {

    public List<Packet> toPacket(StructuredCommand command, byte[] sourceId, byte[] targetId) throws IOException {
        List<Packet> packets = new LinkedList<>();
        byte[] body = serialise(command);
        Packet packet = new Packet(body, sourceId, targetId, PacketType.COMMAND_PACKAGE.type, PacketFlags.ACK.flagValue);
        packets.add(packet);
        return packets;
    }

    private byte[] serialise(StructuredCommand command) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(command);
        return byteArrayOutputStream.toByteArray();
    }
}
