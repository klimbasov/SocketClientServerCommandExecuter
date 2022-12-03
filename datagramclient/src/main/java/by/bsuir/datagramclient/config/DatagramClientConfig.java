package by.bsuir.datagramclient.config;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.command.factory.impl.CommandFactoryImpl;
import by.bsuir.instrumental.command.impl.CopyFileCommand;
import by.bsuir.instrumental.command.ui.InputQueuePool;
import by.bsuir.instrumental.command.ui.RawInputStructuredCommandAdapter;
import by.bsuir.instrumental.command.ui.SystemInputProducer;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.*;
import java.util.List;

@Configuration
public class DatagramClientConfig {
    @Value("${client.timing.loopWaiting}")
    private int runnerTimeout;

    @Value("${client.timing.soTimeout}")
    private int soTimeout;

    @Value("${custom.server.address}")
    private String serverInetAddress;
    @Value("${custom.server.hostname}")
    private String serverHostname;
    @Value("${custom.server.port}")
    private int serverPort;


    @Bean
    public StateHolder stateHolder(){
        return new StateHolder().setRunning(true);
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

    @Bean
    public SlftpController controller(IdentificationHolder holder) {
        return new SlftpController(holder, new FileProcessUriQueuePool(), new InputFileRecordUriQueuePool());
    }

    @Bean
    public StructuredCommandPacketMapper commandPacketMapper(IdentificationHolder holder) {
        return new StructuredCommandPacketMapper(holder);
    }

    @Bean
    public IdentificationHolderImpl identificationHolder() {
        IdentificationHolderImpl holder = new IdentificationHolderImpl();
        holder.setId("123412123412");
        return holder;
    }

    @Bean
    public UuidAddressTable addressTable() throws UnknownHostException {
        InetAddress defaultGatewayAddress = InetAddress.getByName(serverInetAddress);
        InetSocketAddress defaultGateway = new InetSocketAddress(defaultGatewayAddress, serverPort);
        UuidAddressTable addressTable = new UuidAddressTable();
        addressTable.setDefaultGateway(defaultGateway);
        return addressTable;
    }

    @Bean
    public UdpSocketIOWrapper socketIOWrapper(UuidAddressTable addressTable) throws SocketException {
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(soTimeout);
        UdpSocketIOWrapper wrapper = new UdpSocketIOWrapper(addressTable);
        wrapper.setSocket(socket);
        return wrapper;
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
}
