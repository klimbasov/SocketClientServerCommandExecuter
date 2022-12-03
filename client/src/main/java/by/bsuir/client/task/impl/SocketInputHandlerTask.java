package by.bsuir.client.task.impl;

import by.bsuir.instrumental.command.ui.InputQueuePool;
import by.bsuir.instrumental.command.ui.RawInputStructuredCommandAdapter;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.QueuePool;
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
    private final InputQueuePool inputPool;
    /**
     * Pool of packets. Inputs from inputs pool will be transmitted to the packages
     */
    private final QueuePool<Packet> packetQueuePool;
    private final RawInputStructuredCommandAdapter adapter;
    private final StructuredCommandPacketMapper commandPacketMapper;
    /**
     * how many times inputPool will be queried to handle input
     */
    @Value("${client.timing.inputReadIterationsPerTaskExecution}")
    private int ITERATION_PER_CALL;

    public SocketInputHandlerTask(
            InputQueuePool inputPool,
            @Qualifier("outputPoll") QueuePool<Packet> packetQueuePool,
            RawInputStructuredCommandAdapter adapter,
            StructuredCommandPacketMapper commandPacketMapper) {
        this.inputPool = inputPool;
        this.packetQueuePool = packetQueuePool;
        this.adapter = adapter;
        this.commandPacketMapper = commandPacketMapper;
    }

    @Override
    public void run() {
        for (int counter = 0; counter < ITERATION_PER_CALL; counter++) {
            inputPool.poll()
                    .ifPresent(s -> commandPacketMapper.toPackets(adapter.toStructuredCommand(s))
                            .forEach(packetQueuePool::offer));
        }
    }
}
