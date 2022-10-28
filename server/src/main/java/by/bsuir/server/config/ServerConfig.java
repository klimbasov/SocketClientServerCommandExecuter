package by.bsuir.server.config;

import by.bsuir.instrumental.command.factory.impl.CommandFactoryImpl;
import by.bsuir.instrumental.command.impl.CopyFileCommand;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.node.identification.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.Pool;
import by.bsuir.instrumental.pool.impl.*;
import by.bsuir.instrumental.slftp.SlftpController;
import by.bsuir.instrumental.slftp.pool.FileProcessUriPool;
import by.bsuir.instrumental.slftp.pool.InputFileRecordUriPool;
import by.bsuir.instrumental.task.runner.TaskRunner;
import by.bsuir.instrumental.task.runner.impl.AsyncOptimizdTaskRunner;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.util.NodeIdBuilder;
import by.bsuir.server.socket.ServerIOWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

@Configuration
public class ServerConfig {

    @Value("${custom.server.connectivity.port}")
    private int port;
    @Value("${custom.server.connectivity.so_timeout}")
    private int soTimeout;

    @Value("${custom.server.timing.loopWaiting}")
    private int runnerTimeout;

    @Bean
    public ServerSocket serverSocket() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(soTimeout);
            return serverSocket;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public SlftpController controller(IdentificationHolder holder){
        return new SlftpController(holder, new FileProcessUriPool(), new InputFileRecordUriPool());
    }

    @Bean
    public IdentificationHolder identificationHolder(){
        IdentificationHolderImpl holder = new IdentificationHolderImpl();
        holder.setId(NodeIdBuilder.getServerId());
        return holder;
    }
    @Bean
    public StructuredCommandPacketMapper commandPacketMapper(IdentificationHolder identificationHolder){
        return new StructuredCommandPacketMapper(identificationHolder);
    }

    @Bean
    public Pool<Packet> inputPacketPool(){
        return new PacketPoolImpl();
    }

    @Bean
    public CommandFactoryImpl commandFactory(SlftpController controller){
        CommandFactoryImpl factory = new CommandFactoryImpl();
        factory.addCommand("copy", new CopyFileCommand(controller));
        return factory;
    }
    @Bean
    public AbstructNodeIOWrapperOtimazedPool nodeIOWrapperPool(ServerIOWrapper wrapper, CommandFactoryImpl factory){
        AbstructNodeIOWrapperOtimazedPool wrapperPool = new AbstructNodeIOWrapperOtimazedPool();
        wrapperPool.offer(wrapper);
        factory.setWrapperPool(wrapperPool);
        return  wrapperPool;
    }

    @Bean(destroyMethod = "destroy")
    public TaskRunner taskRunner(List<Task> tasks){
        AsyncOptimizdTaskRunner runner = new AsyncOptimizdTaskRunner(tasks.toArray(new Task[0]));
        runner.setSleepTime(runnerTimeout);
        return runner;
    }
}
