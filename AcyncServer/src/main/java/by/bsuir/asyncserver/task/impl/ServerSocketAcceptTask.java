package by.bsuir.asyncserver.task.impl;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.ftp.FtpController;
import by.bsuir.instrumental.ftp.slftp.SlftpController;
import by.bsuir.instrumental.input.StructuredCommandPacketMapper;
import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.node.identification.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.pool.QueuePool;
import by.bsuir.instrumental.pool.impl.AbstractNodeIOWWrapperRingSearchablePool;
import by.bsuir.instrumental.state.application.StateHolder;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.util.NodeIdBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

@RequiredArgsConstructor
@Slf4j
public class ServerSocketAcceptTask implements Task {
//    private final ServerSocket serverSocket;
//    private final StateHolder stateHolder;
//    private final SlftpController slftpController;
//    private final LinkedList<SocketWrapperSingleThreadTask> socketWrapperSingleThreadTasks = new LinkedList<>();
//    private final CommandFactory commandFactory;
//    private final StructuredCommandPacketMapper mapper;
//    private final int THREADS_MAX_NUM = 10;
//
//    @Override
//    public void run() {
//        while (stateHolder.isRunning()){
//            try {
//                Socket socket = serverSocket.accept();
//                IdentificationHolderImpl holder = new IdentificationHolderImpl();
//                holder.setId(NodeIdBuilder.buildSocketIdServer(socket));
//                SocketIOWrapper wrapper = new SocketIOWrapper(socket, holder);
//                log.info("socket connection established: " + wrapper.getHolder().getIdentifier());
//                if(socketWrapperSingleThreadTasks.size()<THREADS_MAX_NUM){
//                    SocketWrapperSingleThreadTask task = new SocketWrapperSingleThreadTask(wrapper, stateHolder, slftpController, mapper, commandFactory);
//                    socketWrapperSingleThreadTasks.add(task);
//
//                }
//            } catch (SocketTimeoutException ignored) {
//            } catch (IOException e) {
//                log.error(e.getMessage());
//            }
//        }
//    }
private final ServerSocket serverSocket;
    private final AbstractNodeIOWWrapperRingSearchablePool socketIOWrapperQueuePool;
    @Setter
    @Getter
    private long timeToListen = 1;

    private static final int THREAD_SIZE = 10;

    @Override
    public void run() {
        for (int counter = 0; counter < timeToListen; counter++) {
            try {
                Socket socket = serverSocket.accept();
                IdentificationHolderImpl holder = new IdentificationHolderImpl();
                holder.setId(NodeIdBuilder.buildSocketIdServer(socket));
                SocketIOWrapper wrapper = new SocketIOWrapper(socket, holder);
                if(socketIOWrapperQueuePool.size() < THREAD_SIZE){
                    socketIOWrapperQueuePool.offerUnnamed(wrapper);
                    log.info("socket connection established: " + wrapper.getHolder().getIdentifier());
                }else {
                    socket.close();
                    log.warn("socket cannot be processed due to the server is out of free slots");
                }
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
