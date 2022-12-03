package by.bsuir.datagramserver.task.impl;

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
public class SocketReceiveTaskImpl implements Task {
    private final UdpSocketIOWrapper socketIOWrapper;
    private final EndNodeIOWrapper nodeIOWrapper;
    @Setter
    @Getter
    @Value("${custom.server.timing.receive}")
    private int requestsPerCall;

    @Override
    public void run() {
        for (int counter = 0; counter < requestsPerCall && socketIOWrapper.isAvailable(); counter++) {
            nodeIOWrapper.receive().ifPresent(socketIOWrapper::send);
            socketIOWrapper.receive().ifPresent(nodeIOWrapper::send);
        }
    }
}
