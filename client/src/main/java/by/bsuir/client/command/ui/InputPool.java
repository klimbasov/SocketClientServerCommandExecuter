package by.bsuir.client.command.ui;

import by.bsuir.instrumental.pool.Pool;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class InputPool implements Pool<String> {
    private final Queue<String> inputQueue = new ConcurrentLinkedQueue();
    @Override
    public void offer(String obj) {
        inputQueue.offer(obj);
    }

    @Override
    public Optional<String> poll() {
        return Optional.ofNullable(inputQueue.poll());
    }

    @Override
    public boolean isEmpty() {
        return inputQueue.isEmpty();
    }
}
