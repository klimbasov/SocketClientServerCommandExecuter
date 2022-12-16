package by.bsuir.instrumental.pool.impl;

import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.pool.SearchableQueuePool;
import by.bsuir.instrumental.pool.Snapshot;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

@Component
public class AbstractNodeIOWrapperQueuePool implements SearchableQueuePool<String, AbstractNodeIOWrapper>, Snapshot {
    private final LinkedList<AbstractNodeIOWrapper> abstractNodeIOWrapperQueue = new LinkedList<>();
    private final HashMap<String, AbstractNodeIOWrapper> socketIOWrapperStringHashMap = new HashMap<>();

    @Override
    public void offer(AbstractNodeIOWrapper obj) {
        synchronized (this) {
            socketIOWrapperStringHashMap.put(obj.getHolder().getIdentifier(), obj);
            abstractNodeIOWrapperQueue.offer(obj);
        }
    }

    @Override
    public Optional<AbstractNodeIOWrapper> poll() {
        synchronized (this) {
            Optional<AbstractNodeIOWrapper> socketIOWrapper = Optional.ofNullable(abstractNodeIOWrapperQueue.poll());
            socketIOWrapper.ifPresent(socketIOWrapper1 -> socketIOWrapperStringHashMap.remove(socketIOWrapper1.getHolder().getIdentifier()));
            return socketIOWrapper;
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return abstractNodeIOWrapperQueue.isEmpty();
        }
    }

    @Override
    public Optional<AbstractNodeIOWrapper> find(String id) {
        synchronized (this) {
            return Optional.ofNullable(socketIOWrapperStringHashMap.get(id));
        }
    }

    @Override
    public Optional<AbstractNodeIOWrapper> remove(String id) {
        synchronized (this) {
            AbstractNodeIOWrapper wrapper = socketIOWrapperStringHashMap.remove(id);
            abstractNodeIOWrapperQueue.remove(wrapper);
            return Optional.ofNullable(wrapper);
        }
    }

    @Override
    public String snapshot() {
        synchronized (this) {
            return abstractNodeIOWrapperQueue.stream().map(wrapper -> wrapper.getHolder().getIdentifier()).reduce((s, s2) -> s + ("\n" + s2)).orElse("no clients can be showed");
        }
    }
}
