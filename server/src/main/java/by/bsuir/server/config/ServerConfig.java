package by.bsuir.server.config;

import by.bsuir.server.Pool;
import by.bsuir.server.task.Task;
import by.bsuir.server.task.TaskPoolImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

@Configuration
public class ServerConfig {

    @Value("${custom.server.port}")
    private int port;
    @Value("${custom.server.so_timeout}")
    private int soTimeout;

    @Bean
    public ServerSocket serverSocket() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(soTimeout);
            return serverSocket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Primary
    public Pool<Task> asyncTaskRunner(List<Task> tasks) {
        Pool<Task> taskPool = new TaskPoolImpl();
        tasks.forEach(taskPool::offer);
        return taskPool;
    }
}
