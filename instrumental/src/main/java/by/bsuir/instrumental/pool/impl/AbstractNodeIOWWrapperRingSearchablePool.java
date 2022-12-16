package by.bsuir.instrumental.pool.impl;

import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.pool.Snapshot;

import java.util.*;

public class AbstractNodeIOWWrapperRingSearchablePool implements Snapshot {
    private static final int INIT_CAPACITY = 20;
    private final ArrayList<AbstractNodeIOWrapper> wrappers = new ArrayList<>(INIT_CAPACITY);
    private final HashMap<String, AbstractNodeIOWrapper> stringAbstractNodeIOWrapperHashMap = new HashMap<>();
    private int placeholderNamed = 0;

    public void offerUnnamed(AbstractNodeIOWrapper obj) {
        synchronized (this){
            wrappers.add(obj);
        }
    }

    public Optional<AbstractNodeIOWrapper> getNext() {
        synchronized (this){
            if (placeholderNamed >= wrappers.size()) {
                placeholderNamed = 0;
            }
            return Optional.of(wrappers.get(placeholderNamed++));
        }
    }

    public boolean isEmpty() {
        synchronized (this){
            return wrappers.isEmpty();
        }
    }

    public long size() {
        synchronized (this){
            return wrappers.size();
        }
    }

    public Optional<AbstractNodeIOWrapper> remove(AbstractNodeIOWrapper wrapper){
        List<String> removedNames = new LinkedList<>();
        synchronized (this){
            for (Map.Entry<String, AbstractNodeIOWrapper> entry : stringAbstractNodeIOWrapperHashMap.entrySet()){
                if(entry.getValue().equals(wrapper)){
                    removedNames.add(entry.getKey());
                }
            }
            removedNames.forEach(stringAbstractNodeIOWrapperHashMap::remove);
            Optional<AbstractNodeIOWrapper> optional = Optional.empty();
            if(wrappers.remove(wrapper)){
                optional = Optional.of(wrapper);
            }
            return optional;
        }
    }

    public void setName(String key, AbstractNodeIOWrapper value){
        synchronized (this){
            if(!wrappers.contains(value)){
                wrappers.add(value);
            }
            stringAbstractNodeIOWrapperHashMap.put(key, value);
        }
    }

    public Optional<AbstractNodeIOWrapper> find(String id) {
        synchronized (this){
            return Optional.ofNullable(stringAbstractNodeIOWrapperHashMap.get(id));
        }
    }

    @Override
    public String snapshot() {
        synchronized (this) {
            return wrappers.stream().map(wrapper -> wrapper.getHolder().getIdentifier()).reduce((s, s2) -> s + ("\n" + s2)).orElse("no clients can be showed");
        }
    }
}
