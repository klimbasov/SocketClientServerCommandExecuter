package by.bsuir.client.config;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.command.factory.impl.CommandFactoryImpl;
import by.bsuir.instrumental.command.impl.CopyFileCommand;
import by.bsuir.instrumental.command.ui.InputQueuePool;
import by.bsuir.instrumental.command.ui.RawInputStructuredCommandAdapter;
import by.bsuir.instrumental.command.ui.SystemInputProducer;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.EndNodeIOWrapper;
import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.node.identification.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.pool.impl.PacketQueuePoolImpl;
import by.bsuir.instrumental.ftp.slftp.SlftpController;
import by.bsuir.instrumental.ftp.slftp.pool.FileProcessUriQueuePool;
import by.bsuir.instrumental.ftp.slftp.pool.InputFileRecordUriQueuePool;
import by.bsuir.instrumental.state.application.StateHolder;
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

    @Value("${client.hostname}")
    private String hostname;
    @Value("${client.timing.loopWaiting}")
    private int runnerTimeout;

    @Bean
    public StateHolder stateHolder(){
        return new StateHolder().setRunning(true);
    }
    @Bean
    public SlftpController controller(IdentificationHolder holder) {
        return new SlftpController(holder, new FileProcessUriQueuePool(), new InputFileRecordUriQueuePool());
    }

    @Bean
    @Qualifier("inputQueuePool")
    public PacketQueuePoolImpl inputPacketPool() {
        return new PacketQueuePoolImpl();
    }

    @Bean
    @Qualifier("outputPoll")
    public PacketQueuePoolImpl outputPacketPool() {
        return new PacketQueuePoolImpl();
    }

    @Bean
    public StructuredCommandPacketMapper commandPacketMapper(IdentificationHolder holder) {
        return new StructuredCommandPacketMapper(holder);
    }

    @Bean
    public IdentificationHolderImpl identificationHolder() {
        IdentificationHolderImpl holder = new IdentificationHolderImpl();
        holder.setId(hostname);
        return holder;
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
    public TaskRunner taskRunner(List<Task> tasks, StateHolder stateHolder) {
        AsyncOptimizdTaskRunner runner = new AsyncOptimizdTaskRunner(tasks.toArray(new Task[0]), stateHolder);
        runner.setSleepTime(runnerTimeout);
        return runner;
    }

    @Bean
    public InputQueuePool inputQueuePool(){
        return new InputQueuePool();
    }

    @Bean
    public RawInputStructuredCommandAdapter rawInputStructuredCommandAdapter(){
        return new RawInputStructuredCommandAdapter();
    }

    @Bean
    public SystemInputProducer systemInputProducer(StateHolder holder, InputQueuePool pool){
        return new SystemInputProducer(holder, pool);
    }
}
