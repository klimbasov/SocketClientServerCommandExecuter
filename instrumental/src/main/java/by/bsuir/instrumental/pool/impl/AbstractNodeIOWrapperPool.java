package by.bsuir.instrumental.pool.impl;

import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.pool.SearchablePool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Component
public class AbstractNodeIOWrapperPool implements SearchablePool<String, AbstractNodeIOWrapper> {
    private final Queue<AbstractNodeIOWrapper> abstractNodeIOWrapperQueue = new LinkedList<>();
    private final HashMap<String, AbstractNodeIOWrapper> socketIOWrapperStringHashMap = new HashMap<>();

    @Override
    public void offer(AbstractNodeIOWrapper obj) {
        socketIOWrapperStringHashMap.put(obj.getSocketId(), obj);
        abstractNodeIOWrapperQueue.offer(obj);
    }

    @Override
    public Optional<AbstractNodeIOWrapper> poll() {
        Optional<AbstractNodeIOWrapper> socketIOWrapper = Optional.ofNullable(abstractNodeIOWrapperQueue.poll());
        socketIOWrapper.ifPresent(socketIOWrapper1 -> socketIOWrapperStringHashMap.remove(socketIOWrapper1.getSocketId()));
        return socketIOWrapper;
    }

    @Override
    public boolean isEmpty() {
        return abstractNodeIOWrapperQueue.isEmpty();
    }

    @Override
    public Optional<AbstractNodeIOWrapper> find(String id) {
        return Optional.ofNullable(socketIOWrapperStringHashMap.get(id));
    }
}
