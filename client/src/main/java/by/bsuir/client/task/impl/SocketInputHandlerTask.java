package by.bsuir.client.task.impl;

import by.bsuir.client.command.ui.InputPool;
import by.bsuir.client.command.ui.RawInputStructuredCommandAdapter;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.task.Task;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Map user Input to command packets
 */
@Component
public class SocketInputHandlerTask implements Task {
    /**
     * Pool of raw user input
     */
    private final InputPool inputPool;
    /**
     * Pool of packets. Inputs from inputs pool will be transmitted to the packages
     */
    private final Pool<Packet> packetPool;
    /**
     * how many times inputPool will be queried to handle input
     */
    @Value("${client.timing.inputReadIterationsPerTaskExecution}")
    private int ITERATION_PER_CALL;

    private final RawInputStructuredCommandAdapter adapter;
    private final StructuredCommandPacketMapper commandPacketMapper;

    public SocketInputHandlerTask(
            InputPool inputPool,
            @Qualifier("outputPoll") Pool<Packet> packetPool,
            RawInputStructuredCommandAdapter adapter,
            StructuredCommandPacketMapper commandPacketMapper) {
        this.inputPool = inputPool;
        this.packetPool = packetPool;
        this.adapter = adapter;
        this.commandPacketMapper = commandPacketMapper;
    }

    @Override
    public void run() {
        for (int counter = 0; counter < ITERATION_PER_CALL; counter++){
            inputPool.poll()
                    .ifPresent(s -> commandPacketMapper.toPackets(adapter.toStructuredCommand(s))
                            .forEach(packetPool::offer));
        }
    }
}
