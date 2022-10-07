package by.bsuir.client.task.impl;

import by.bsuir.client.command.ui.InputPool;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.task.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InputHandlerTask implements Task {
    private final InputPool inputPool;
    private final Pool<Packet> packetPool;
    private static final int ITERATION_PER_CALL = 5;
    @Override
    public void run() {
        for (int counter = 0; counter < ITERATION_PER_CALL; counter++){
            inputPool.poll().ifPresent(s -> {

            });
        }
    }
}
