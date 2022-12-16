package by.bsuir.asyncserver.config;

import by.bsuir.asyncserver.pool.MultithreadingSocketHandlerTaskPool;
import by.bsuir.asyncserver.task.impl.MultithreadingSocketHandlerTask;
import by.bsuir.asyncserver.task.impl.ServerSocketAcceptTask;
import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.command.factory.impl.CommandFactoryImpl;
import by.bsuir.instrumental.command.impl.CopyFileCommand;
import by.bsuir.instrumental.ftp.slftp.SlftpController;
import by.bsuir.instrumental.ftp.slftp.pool.FileProcessUriQueuePool;
import by.bsuir.instrumental.ftp.slftp.pool.InputFileRecordUriQueuePool;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.EndNodeIOWrapper;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.node.identification.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWWrapperRingSearchablePool;
import by.bsuir.instrumental.pool.impl.PacketQueuePoolImpl;
import by.bsuir.instrumental.state.application.StateHolder;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.task.runner.TaskRunner;
import by.bsuir.instrumental.task.runner.impl.AsyncOptimizdTaskRunner;
import by.bsuir.instrumental.util.NodeIdBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Value("${custom.server.scale.pool-max-size}")
    private int threadPoolMaxSize;

    @Value("${custom.server.connectivity.id}")
    private String hostname;

    @Bean
    public StateHolder stateHolder() {
        return new StateHolder().setRunning(true);
    }

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
    public PacketQueuePoolImpl inputPacketPool() {
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
        wrapperPool.offerUnnamed(wrapper);
        wrapperPool.setName(hostname, wrapper);
        factory.setWrapperPool(wrapperPool);
        return wrapperPool;
    }

    @Bean
    public MultithreadingSocketHandlerTaskPool multithreadingSocketHandlerTaskPool(StateHolder stateHolder, EndNodeIOWrapper endNodeIOWrapper, PacketQueuePoolImpl queuePool) {
        MultithreadingSocketHandlerTaskPool pool = new MultithreadingSocketHandlerTaskPool(stateHolder, threadPoolMaxSize);
        MultithreadingSocketHandlerTask endNodeTask = new MultithreadingSocketHandlerTask(endNodeIOWrapper, stateHolder, queuePool);
        endNodeTask.setUuid(hostname);
        if (!pool.offer(endNodeTask)) {
            throw new RuntimeException("Could not add end node element inf task to poll");
        }
        return pool;
    }

    @Bean(name = "tasks")
    public List<Task> tasks(ServerSocket socket, MultithreadingSocketHandlerTaskPool multithreadingSocketHandlerTaskPool,
                            PacketQueuePoolImpl packetQueuePool,
                            StateHolder stateHolder) {
        Task serverSocketAccept = new ServerSocketAcceptTask(socket, multithreadingSocketHandlerTaskPool, packetQueuePool, stateHolder);
        return List.of(serverSocketAccept);
    }

    @Bean(destroyMethod = "destroy")
    public TaskRunner taskRunner(@Qualifier("tasks") List<Task> tasks, StateHolder stateHolder) {
        AsyncOptimizdTaskRunner runner = new AsyncOptimizdTaskRunner(tasks.toArray(new Task[0]), stateHolder);
        runner.setSleepTime(runnerTimeout);
        return runner;
    }
}
