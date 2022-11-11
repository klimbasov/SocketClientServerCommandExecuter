package by.bsuir.server.config;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.command.factory.impl.CommandFactoryImpl;
import by.bsuir.instrumental.command.impl.CopyFileCommand;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.EndNodeIOWrapper;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.node.identification.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.QueuePool;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWWrapperRingSearchablePool;
import by.bsuir.instrumental.pool.impl.PacketQueuePoolImpl;
import by.bsuir.instrumental.slftp.SlftpController;
import by.bsuir.instrumental.slftp.pool.FileProcessUriQueuePool;
import by.bsuir.instrumental.slftp.pool.InputFileRecordUriQueuePool;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.task.runner.TaskRunner;
import by.bsuir.instrumental.task.runner.impl.AsyncOptimizdTaskRunner;
import by.bsuir.instrumental.util.NodeIdBuilder;
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
    public SlftpController controller(IdentificationHolder holder) {
        return new SlftpController(holder, new FileProcessUriQueuePool(), new InputFileRecordUriQueuePool());
    }

    @Bean
    public IdentificationHolder identificationHolder() {
        IdentificationHolderImpl holder = new IdentificationHolderImpl();
        holder.setId(NodeIdBuilder.getServerId());
        return holder;
    }

    @Bean
    public StructuredCommandPacketMapper commandPacketMapper(IdentificationHolder identificationHolder) {
        return new StructuredCommandPacketMapper(identificationHolder);
    }

    @Bean
    public QueuePool<Packet> inputPacketPool() {
        return new PacketQueuePoolImpl();
    }

    @Bean
    public CommandFactoryImpl commandFactory(SlftpController controller, IdentificationHolder holder) {
        CommandFactoryImpl factory = new CommandFactoryImpl();
        factory.addCommand("copy", new CopyFileCommand(controller, holder));
        return factory;
    }

    @Bean
    public EndNodeIOWrapper endNodeIOWrapper(IdentificationHolder holder,
                                             StructuredCommandPacketMapper mapper,
                                             CommandFactory factory,
                                             SlftpController slftpController) {
        return new EndNodeIOWrapper(holder, mapper, factory, slftpController);
    }

    @Bean
    public AbstractNodeIOWWrapperRingSearchablePool nodeIOWrapperPool(EndNodeIOWrapper wrapper, CommandFactoryImpl factory) {
        AbstractNodeIOWWrapperRingSearchablePool wrapperPool = new AbstractNodeIOWWrapperRingSearchablePool();
        wrapperPool.offer(wrapper);
        factory.setWrapperPool(wrapperPool);
        return wrapperPool;
    }

    @Bean(destroyMethod = "destroy")
    public TaskRunner taskRunner(List<Task> tasks) {
        AsyncOptimizdTaskRunner runner = new AsyncOptimizdTaskRunner(tasks.toArray(new Task[0]));
        runner.setSleepTime(runnerTimeout);
        return runner;
    }
}
