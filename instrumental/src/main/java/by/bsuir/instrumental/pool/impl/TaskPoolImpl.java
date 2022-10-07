package by.bsuir.instrumental.pool.impl;

import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.task.Task;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Component
public class TaskPoolImpl implements Pool<Task> {
    private final Queue<Task> tasks = new LinkedList<>();

    @Override
    public void offer(Task obj) {
        tasks.offer(obj);
    }

    @Override
    public Optional<Task> poll() {
        return Optional.ofNullable(tasks.poll());
    }

    @Override
    public boolean isEmpty() {
        return tasks.isEmpty();
    }
}
