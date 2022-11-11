package by.bsuir.instrumental.node;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.slftp.SlftpController;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Component
@Slf4j
public class EndNodeIOWrapper extends AbstractNodeIOWrapper {

    private final SlftpController controller;
    private final StructuredCommandPacketMapper processor;
    private final CommandFactory commandFactory;
    private final Queue<Packet> packetQueue = new LinkedList<>();
    private static final int MAX_IDEL = 300;
    private int idel = 0;

    public EndNodeIOWrapper(IdentificationHolder holder, StructuredCommandPacketMapper processor, CommandFactory commandFactory, SlftpController controller) {
        super(holder);
        this.processor = processor;
        this.commandFactory = commandFactory;
        this.controller = controller;
    }

    @Override
    public Optional<Packet> receive() {
        ++idel;
        if(idel >= MAX_IDEL){
            log.warn("Node spend too much time in idel state. Take attention.");
            idel = 0;
        }
        Optional<Packet> optional = Optional.ofNullable(packetQueue.poll());
        if (optional.isEmpty()) {
            optional = Optional.ofNullable(controller.receive());
        }
        return optional;
    }

    @Override
    public void send(Packet packet) {
        idel = 0;
        PacketType type = PacketType.getInstance(packet.getType());
        switch (type) {
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
        System.out.println(new String(packet.getBody()));
    }

    private void commandPacketHandler(Packet packet) {
        List<StructuredCommand> structuredCommandList = processor.toStructuredCommand(List.of(packet));
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
