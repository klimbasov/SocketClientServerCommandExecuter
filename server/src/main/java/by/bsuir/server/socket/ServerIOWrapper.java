package by.bsuir.server.socket;

import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.slftp.SlftpController;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ServerIOWrapper extends AbstractNodeIOWrapper {
    private final SlftpController controller;
    private final StructuredCommandPacketMapper commandPacketMapper;
    private final CommandFactory commandFactory;
    private final Queue<Packet> packetQueue = new LinkedList<>();

    public ServerIOWrapper(StructuredCommandPacketMapper structuredCommandPacketMapper, CommandFactory commandFactory, IdentificationHolder holder, SlftpController controller) {
        super(holder);
        this.commandPacketMapper = structuredCommandPacketMapper;
        this.commandFactory = commandFactory;
        this.controller = controller;
    }

    @Override
    public Optional<Packet> receive() {
        Optional<Packet> optional = Optional.ofNullable(packetQueue.poll());
        if(optional.isEmpty()){
            optional = Optional.ofNullable(controller.receive());
        }
        return optional;
    }

    @Override
    public void send(Packet packet) {
        PacketType type = PacketType.getInstance(packet.getType());
        switch (type){
            case COMMAND_PACKAGE -> commandPacketHandler(packet);
            case INFORM_PACKAGE -> informPacketHandler(packet);
            case SLFTP_PACKAGE -> slftpPackageHandler(packet);

        }
    }

    private void slftpPackageHandler(Packet packet) {
        controller.handleRequest(packet);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private void informPacketHandler(Packet packet) {
        System.out.println(Arrays.toString(packet.getBody()));
    }

    private void commandPacketHandler(Packet packet) {
        List<StructuredCommand> structuredCommandList = commandPacketMapper.toStructuredCommand(List.of(packet));
        List<Packet> results = structuredCommandList.stream()
                .map(structuredCommand -> {
                    String response = commandFactory.execute(structuredCommand);
                    return new Packet(
                            response.getBytes(),
                            packet.getTargetId(),
                            packet.getSourceId(),
                            PacketType.INFORM_PACKAGE.typeId,
                            PacketFlags.ACK.flagValue);
                }).toList();
        results.forEach(packetQueue::offer);
    }

    @Override
    public void close() {

    }
}
