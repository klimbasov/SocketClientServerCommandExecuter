package by.bsuir.datagramclient.task.impl;

import by.bsuir.instrumental.command.ui.InputQueuePool;
import by.bsuir.instrumental.command.ui.RawInputStructuredCommandAdapter;
import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.UdpSocketIOWrapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.task.Task;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Map user Input to command packets
 */
@Component
public class SocketInputHandlerTask implements Task {
    /**
     * Pool of raw user input
     */
    private final InputQueuePool inputPool;
    /**
     * Pool of packets. Inputs from inputs pool will be transmitted to the packages
     */
    private final RawInputStructuredCommandAdapter adapter;
    private final UdpSocketIOWrapper wrapper;
    private final StructuredCommandPacketMapper commandPacketMapper;
    /**
     * how many times inputPool will be queried to handle input
     */
    @Value("${client.timing.input}")
    private int ITERATION_PER_CALL;

    public SocketInputHandlerTask(
            InputQueuePool inputPool,
            RawInputStructuredCommandAdapter adapter,
            UdpSocketIOWrapper wrapper,
            StructuredCommandPacketMapper commandPacketMapper) {
        this.inputPool = inputPool;
        this.wrapper = wrapper;
        this.adapter = adapter;
        this.commandPacketMapper = commandPacketMapper;
    }

    @Override
    public void run() {
        for (int counter = 0; counter < ITERATION_PER_CALL; counter++) {
            List<StructuredCommand> structuredCommands = inputPool.pollAll().stream().map(adapter::toStructuredCommand).flatMap(List::stream).toList();
            List<Packet> packets = commandPacketMapper.toPackets(structuredCommands);
            wrapper.send(packets);
        }
    }
}
