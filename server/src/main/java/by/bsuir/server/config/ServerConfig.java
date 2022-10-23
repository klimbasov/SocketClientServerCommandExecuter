package by.bsuir.server.config;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.command.factory.impl.CommandFactoryImpl;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.node.token.IdentificationHolder;
import by.bsuir.instrumental.node.token.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWrapperPool;
import by.bsuir.instrumental.pool.impl.PacketPoolImpl;
import by.bsuir.instrumental.task.AsyncTaskRunner;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.pool.impl.TaskPoolImpl;
import by.bsuir.server.socket.ServerIOWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
    public Pool<Task> taskPool(List<Task> tasks) {
        Pool<Task> taskPool = new TaskPoolImpl();
        tasks.forEach(taskPool::offer);
        return taskPool;
    }

    @Bean
    public IdentificationHolder identificationHolder(){
        return new IdentificationHolderImpl();
    }
    @Bean
    public StructuredCommandPacketMapper commandPacketMapper(IdentificationHolder identificationHolder){
        return new StructuredCommandPacketMapper(identificationHolder);
    }

    @Bean
    public Pool<Packet> packetPool(){
        return new PacketPoolImpl();
    }

    @Bean
    public CommandFactory commandFactory(){
        return new CommandFactoryImpl();
    }
    @Bean
    public AbstractNodeIOWrapperPool nodeIOWrapperPool(ServerIOWrapper serverIOWrapper){
        AbstractNodeIOWrapperPool nodeIOWrapperPool = new AbstractNodeIOWrapperPool();
        nodeIOWrapperPool.offer(serverIOWrapper);
        return nodeIOWrapperPool;
    }
    @Bean(destroyMethod = "destroy")
    public AsyncTaskRunner asyncTaskRunner(Pool<Task> taskPool){
        return new AsyncTaskRunner(taskPool);
    }
}
