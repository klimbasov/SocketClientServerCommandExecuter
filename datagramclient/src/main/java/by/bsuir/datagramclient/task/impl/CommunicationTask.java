package by.bsuir.datagramclient.task.impl;

import by.bsuir.instrumental.node.EndNodeIOWrapper;
import by.bsuir.instrumental.node.UdpSocketIOWrapper;
import by.bsuir.instrumental.task.Task;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunicationTask implements Task {
    private final UdpSocketIOWrapper socketIOWrapper;
    private final EndNodeIOWrapper nodeIOWrapper;
    @Setter
    @Getter
    @Value("${client.timing.receive}")
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && socketIOWrapper.isAvailable(); counter++) {
            socketIOWrapper.send(nodeIOWrapper.receive());
            nodeIOWrapper.send(socketIOWrapper.receive());
        }
    }
}
