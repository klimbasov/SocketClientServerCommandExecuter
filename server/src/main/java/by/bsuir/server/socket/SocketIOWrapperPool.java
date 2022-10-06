package by.bsuir.server.socket;

import by.bsuir.server.Pool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Component
public class SocketIOWrapperPool implements Pool<SocketIOWrapper>, SearchableSocketIOWrapperPool {
    private final Queue<SocketIOWrapper> socketIOWrapperQueue = new LinkedList<>();
    private final HashMap<String, SocketIOWrapper> socketIOWrapperStringHashMap = new HashMap<>();

    @Override
    public void offer(SocketIOWrapper obj) {
        socketIOWrapperStringHashMap.put(obj.getSocketId(), obj);
        socketIOWrapperQueue.offer(obj);
    }

    @Override
    public Optional<SocketIOWrapper> poll() {
        Optional<SocketIOWrapper> socketIOWrapper = Optional.ofNullable(socketIOWrapperQueue.poll());
        socketIOWrapper.ifPresent(socketIOWrapper1 -> socketIOWrapperStringHashMap.remove(socketIOWrapper1.getSocketId()));
        return socketIOWrapper;
    }

    @Override
    public boolean isEmpty() {
        return socketIOWrapperQueue.isEmpty();
    }

    @Override
    public Optional<SocketIOWrapper> find(String id) {
        return Optional.ofNullable(socketIOWrapperStringHashMap.get(id));
    }
}
