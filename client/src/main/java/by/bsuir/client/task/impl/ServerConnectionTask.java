package by.bsuir.client.task.impl;

import by.bsuir.instrumental.node.SocketIOWrapper;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import by.bsuir.instrumental.node.identification.impl.IdentificationHolderImpl;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.util.NodeIdBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServerConnectionTask implements Task {
    private final IdentificationHolderImpl holder;
    private final SocketIOWrapper currentSocketIOWrapper;

    @Value("${client.connectivity.ip}")
    private String ip;
    @Value("${client.connectivity.port}")
    private int port;

    @Override
    public void run() {
        if(!currentSocketIOWrapper.isAvailable()){
            try {
                currentSocketIOWrapper.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                Socket socket = new Socket(ip, port);
                holder.setId(NodeIdBuilder.buildSocketIdClient(socket));
                log.info("external socket id : " + socket.getInetAddress());
                currentSocketIOWrapper.setSocket(socket);
            } catch (IOException e) {
            }
        }
    }
}
