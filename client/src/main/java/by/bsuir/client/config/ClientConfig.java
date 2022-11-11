package by.bsuir.client.config;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.command.factory.impl.CommandFactoryImpl;
import by.bsuir.instrumental.command.impl.CopyFileCommand;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.EndNodeIOWrapper;
import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.node.identification.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.pool.QueuePool;
import by.bsuir.instrumental.pool.impl.PacketQueuePoolImpl;
import by.bsuir.instrumental.slftp.SlftpController;
import by.bsuir.instrumental.slftp.pool.FileProcessUriQueuePool;
import by.bsuir.instrumental.slftp.pool.InputFileRecordUriQueuePool;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.task.runner.TaskRunner;
import by.bsuir.instrumental.task.runner.impl.AsyncOptimizdTaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class ClientConfig {
    @Value("${client.timing.loopWaiting}")
    private int runnerTimeout;

    @Bean
    public SlftpController controller(IdentificationHolder holder) {
        return new SlftpController(holder, new FileProcessUriQueuePool(), new InputFileRecordUriQueuePool());
    }

    @Bean
    @Qualifier("inputQueuePool")
    public QueuePool<Packet> inputPacketPool() {
        return new PacketQueuePoolImpl();
    }

    @Bean
    @Qualifier("outputPoll")
    public QueuePool<Packet> outputPacketPool() {
        return new PacketQueuePoolImpl();
    }

    @Bean
    public StructuredCommandPacketMapper commandPacketMapper(IdentificationHolder holder) {
        return new StructuredCommandPacketMapper(holder);
    }

    @Bean
    public IdentificationHolderImpl identificationHolder() {
        return new IdentificationHolderImpl();
    }

    @Bean
    public SocketIOWrapper socketIOWrapper(IdentificationHolder holder) {
        return new SocketIOWrapper(holder);
    }

    @Bean
    public EndNodeIOWrapper endNodeIOWrapper(IdentificationHolder holder,
                                             StructuredCommandPacketMapper mapper,
                                             CommandFactory factory,
                                             SlftpController controller) {
        return new EndNodeIOWrapper(holder, mapper, factory, controller);
    }

    @Bean
    public CommandFactory commandFactory(SlftpController controller, IdentificationHolder holder) {
        CommandFactoryImpl factory = new CommandFactoryImpl();
        factory.addCommand("copy", new CopyFileCommand(controller, holder));
        return factory;
    }

    @Bean(destroyMethod = "destroy")
    public TaskRunner taskRunner(List<Task> tasks) {
        AsyncOptimizdTaskRunner runner = new AsyncOptimizdTaskRunner(tasks.toArray(new Task[0]));
        runner.setSleepTime(runnerTimeout);
        return runner;
    }

}
