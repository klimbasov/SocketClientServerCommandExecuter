package by.bsuir.instrumental.node;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.ftp.FtpController;
import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.state.socket.ThreadStateHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
public class SyncExecThreadTaskDriverNode {
    private final FtpController controller;
    private final StructuredCommandPacketMapper processor;
    private final CommandFactory commandFactory;
    private final Queue<Packet> packetQueue = new LinkedList<>();
    private static final int MAX_IDEL = 900;
    private int idel = 0;

    public SyncExecThreadTaskDriverNode(StructuredCommandPacketMapper processor, CommandFactory commandFactory, FtpController controller){
        this.processor = processor;
        this.commandFactory = commandFactory;
        this.controller = controller;
    }

    public List<Packet> process(List<Packet> packets, ThreadStateHolder threadStateHolder){
        packets.forEach(this::packetHandler);
        if (packetQueue.isEmpty()) {
            packetQueue.addAll(controller.receive());
        }
        List<Packet> responses = new ArrayList<>(packetQueue);
        packetQueue.clear();
        return responses;
    }

    private void packetHandler(Packet packet){
        PacketType type = PacketType.getInstance(packet.getType());
        switch (type) {
            case COMMAND_PACKAGE -> commandPacketHandler(packet);
            case INFORM_PACKAGE -> informPacketHandler(packet);
            case FTP_PACKAGE -> slftpPackageHandler(packet);
        }
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

    private void slftpPackageHandler(Packet packet) {
        controller.send(packet);
    }
}
