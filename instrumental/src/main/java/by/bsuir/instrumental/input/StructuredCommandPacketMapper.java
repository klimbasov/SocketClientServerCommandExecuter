package by.bsuir.instrumental.input;

import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.DisposableBean;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class StructuredCommandPacketMapper implements DisposableBean
{
    private final IdentificationHolder identificationHolder;

    public StructuredCommandPacketMapper(IdentificationHolder identificationHolder) {
        this.identificationHolder = identificationHolder;
//        byteArrayOutputStream = new ByteArrayOutputStream();
//        try {
//            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public List<Packet> toPackets(List<StructuredCommand> structuredCommandList) {
        List<Packet> packets = new LinkedList<>();
        for (StructuredCommand command :
                structuredCommandList) {
            Packet packet = new Packet(serialise(command),
                    identificationHolder.getIdentifier().getBytes(),
                    command.getTargetIdentifier().getBytes(),
                    PacketType.COMMAND_PACKAGE.typeId,
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
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        ){
            objectOutputStream.writeObject(command);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private StructuredCommand deserialize(byte[] serializedCommand){
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedCommand);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);){
            return (StructuredCommand) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void destroy() throws Exception {
//        objectOutputStream.close();
//        byteArrayOutputStream.close();
    }
}
