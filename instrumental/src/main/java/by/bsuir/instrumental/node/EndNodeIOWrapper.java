package by.bsuir.instrumental.node;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.ftp.FtpController;
import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class EndNodeIOWrapper extends AbstractNodeIOWrapper {

    private final FtpController controller;
    private final StructuredCommandPacketMapper processor;
    private final CommandFactory commandFactory;
    private final Queue<Packet> packetQueue = new LinkedList<>();
    private static final int MAX_IDEL = 900;
    private int idel = 0;

    public EndNodeIOWrapper(IdentificationHolder holder, StructuredCommandPacketMapper processor, CommandFactory commandFactory, FtpController controller) {
        super(holder);
        this.processor = processor;
        this.commandFactory = commandFactory;
        this.controller = controller;
    }

    @Override
    public List<Packet> receive() {
        ++idel;
        if(idel >= MAX_IDEL){
            log.warn("Node spend too much time in idel state. Take attention.");
            idel = 0;
        }
        if (packetQueue.isEmpty()) {
            packetQueue.addAll(controller.receive());
        }
        if(!packetQueue.isEmpty()){
            idel = 0;
        }
        List<Packet> packets = new ArrayList<>(packetQueue);
        packetQueue.clear();
        return packets;
    }

    @Override
    public void send(List<Packet> packets) {
        idel = 0;
        packets.forEach(this::packetHandler);
    }

    private void packetHandler(Packet packet){
        PacketType type = PacketType.getInstance(packet.getType());
        switch (type) {
            case COMMAND_PACKAGE -> commandPacketHandler(packet);
            case INFORM_PACKAGE -> informPacketHandler(packet);
            case FTP_PACKAGE -> slftpPackageHandler(packet);
        }
    }

    private void slftpPackageHandler(Packet packet) {
        controller.send(packet);
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
        controller.close();
    }
}
