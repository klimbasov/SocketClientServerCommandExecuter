package by.bsuir.client.config;

import by.bsuir.client.socket.impl.ClientIOWrapper;
import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.command.factory.impl.CommandFactoryImpl;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.node.token.IdentificationHolder;
import by.bsuir.instrumental.node.token.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWrapperPool;
import by.bsuir.instrumental.pool.impl.PacketPoolImpl;
import by.bsuir.instrumental.pool.impl.TaskPoolImpl;
import by.bsuir.instrumental.task.AsyncTaskRunner;
import by.bsuir.instrumental.task.Task;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

@Configuration
public class ClientConfig {

    @Value("${client.connectivity.ip}")
    private String ip;
    @Value("${client.connectivity.port}")
    private int port;
    @Bean
    @Primary
    public Pool<Task> taskPool(List<Task> tasks) {
        Pool<Task> taskPool = new TaskPoolImpl();
        tasks.forEach(taskPool::offer);
        return taskPool;
    }

    @Bean
    public Pool<Packet> packetPool(){
        return new PacketPoolImpl();
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
    public SocketIOWrapper socketIOWrapper(){
        try{
            Socket socket = new Socket(ip, port);
            return new SocketIOWrapper(socket);
        }catch (IOException e){
            throw new RuntimeException("socket has been not initialized");
        }
    }


    @Bean
    public CommandFactory commandFactory(){
        return new CommandFactoryImpl();
    }
    @Bean
    @Primary
    public Pool<AbstractNodeIOWrapper> nodeIOWrapperPool(ClientIOWrapper clientIOWrapper, SocketIOWrapper socketIOWrapper){
        AbstractNodeIOWrapperPool nodeIOWrapperPool = new AbstractNodeIOWrapperPool();
        nodeIOWrapperPool.offer(clientIOWrapper);
        nodeIOWrapperPool.offer(socketIOWrapper);
        return nodeIOWrapperPool;
    }

    @Bean(destroyMethod = "destroy")
    public AsyncTaskRunner asyncTaskRunner(Pool<Task> taskPool){
        return new AsyncTaskRunner(taskPool);
    }
}
