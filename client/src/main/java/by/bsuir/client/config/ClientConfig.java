package by.bsuir.client.config;

import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.pool.impl.TaskPoolImpl;
import by.bsuir.instrumental.task.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

public class ClientConfig {
    @Bean
    @Primary
    public Pool<Task> taskPool(List<Task> tasks) {
        Pool<Task> taskPool = new TaskPoolImpl();
        tasks.forEach(taskPool::offer);
        return taskPool;
    }
}
