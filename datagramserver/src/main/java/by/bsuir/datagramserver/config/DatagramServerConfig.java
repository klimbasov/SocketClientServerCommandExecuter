package by.bsuir.datagramserver.config;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.command.factory.impl.CommandFactoryImpl;
import by.bsuir.instrumental.command.impl.CopyFileCommand;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.EndNodeIOWrapper;
import by.bsuir.instrumental.node.UdpSocketIOWrapper;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.node.identification.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.pool.UuidAddressTable;
import by.bsuir.instrumental.ftp.slftp.SlftpController;
import by.bsuir.instrumental.ftp.slftp.pool.FileProcessUriQueuePool;
import by.bsuir.instrumental.ftp.slftp.pool.InputFileRecordUriQueuePool;
import by.bsuir.instrumental.state.StateHolder;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.task.runner.TaskRunner;
import by.bsuir.instrumental.task.runner.impl.AsyncOptimizdTaskRunner;
import by.bsuir.instrumental.util.NodeIdBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.*;
import java.util.List;

@Configuration
@Slf4j
public class DatagramServerConfig {
    @Value("${custom.server.connectivity.port}")
    private int port;
    @Value("${custom.server.connectivity.so_timeout}")
    private int soTimeout;

    @Value("${custom.server.timing.loopWaiting}")
    private int runnerTimeout;

    @Bean
    public StateHolder stateHolder(){
        return new StateHolder().setRunning(true);
    }

    @Bean
    public StructuredCommandPacketMapper commandPacketMapper(IdentificationHolder identificationHolder) {
        return new StructuredCommandPacketMapper(identificationHolder);
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
    public UuidAddressTable addressTable() throws UnknownHostException {
        return new UuidAddressTable();
    }

    @Bean
    public UdpSocketIOWrapper socketIOWrapper(UuidAddressTable addressTable) throws SocketException {
        UdpSocketIOWrapper wrapper = new UdpSocketIOWrapper(addressTable);
        DatagramSocket socket = new DatagramSocket(port);
        socket.setSoTimeout(soTimeout);
        wrapper.setSocket(socket);
        log.info("socket local address: " + socket.getLocalAddress() + " port: " + socket.getLocalPort());
        log.info("socket inet address: " + socket.getInetAddress() + " port: " + socket.getPort());
        return wrapper;
    }

    @Bean(destroyMethod = "destroy")
    public TaskRunner taskRunner(List<Task> tasks, StateHolder stateHolder) {
        AsyncOptimizdTaskRunner runner = new AsyncOptimizdTaskRunner(tasks.toArray(new Task[0]), stateHolder);
        runner.setSleepTime(runnerTimeout);
        return runner;
    }
}
