package by.bsuir.client.socket.impl;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.input.PlainCommandProcessor;
import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Component
@RequiredArgsConstructor
public class ClientIOWrapper extends AbstractNodeIOWrapper {
    private final PlainCommandProcessor processor;
    private final CommandFactory commandFactory;
    private final Queue<Packet> packetQueue;
    public void setSocketId(String socketId){
        super.setSocketId(socketId);
    }

    @Override
    public Optional<Packet> receive() {
        return Optional.ofNullable(packetQueue.poll());
    }

    @Override
    public void send(Packet packet) {
        PacketType type = PacketType.getInstance(packet.getType());
        switch (type){
            case COMMAND_PACKAGE -> commandPacketHandler(packet);
            case INFORM_PACKAGE -> informPacketHandler(packet);
            case DATA_PACKAGE -> dataPackageHandler(packet);

        }
    }

    private void dataPackageHandler(Packet packet) {
    }

    private void informPacketHandler(Packet packet){}

    private void commandPacketHandler(Packet packet) {
        List<StructuredCommand> structuredCommandList = processor.process(new String(packet.getBody()));
        List<Packet> results = structuredCommandList.stream()
                .map(structuredCommand -> {
                    String response = commandFactory.execute(structuredCommand);
                    return new Packet(
                            response.getBytes(),
                            packet.getTargetId(),
                            packet.getSourceId(),
                            PacketType.INFORM_PACKAGE.type,
                            PacketFlags.ACK.flagValue);
                }).toList();
        results.forEach(packetQueue::offer);
    }

    @Override
    public void close() throws Exception {

    }
}
