package by.bsuir.instrumental.pool.impl;

import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.pool.SearchablePool;
import by.bsuir.instrumental.pool.SnapshottingPool;

import java.util.ArrayList;
import java.util.Optional;

public class AbstructNodeIOWrapperOtimazedPool implements SearchablePool<String, AbstractNodeIOWrapper>, SnapshottingPool {
    private static final int INIT_CAPACITY = 20;
    private final ArrayList<AbstractNodeIOWrapper> wrappers = new ArrayList<>(INIT_CAPACITY);
    private int placeholder = 0;

    @Override
    public void offer(AbstractNodeIOWrapper obj) {
        wrappers.add(obj);
    }

    @Override
    public Optional<AbstractNodeIOWrapper> poll() {
        if(placeholder == wrappers.size()){
            placeholder = 0;
        }
        return Optional.of(wrappers.get(placeholder++));
    }

    @Override
    public boolean isEmpty() {
        return wrappers.isEmpty();
    }

    @Override
    public Optional<AbstractNodeIOWrapper> find(String id) {
        Optional<AbstractNodeIOWrapper> optional;
        optional = Optional.ofNullable(findById(id));
        return optional;
    }

    private AbstractNodeIOWrapper findById(String id) {
        AbstractNodeIOWrapper retVal = null;
        AbstractNodeIOWrapper[] innerArray = wrappers.toArray(new AbstractNodeIOWrapper[0]);
        for (AbstractNodeIOWrapper wrapper: innerArray){
            if(wrapper.getHolder().getIdentifier().equals(id)){
                retVal = wrapper;
                break;
            }
        }
        return retVal;
    }

    @Override
    public Optional<AbstractNodeIOWrapper> remove(String id) {
        wrappers.remove(findById(id));
        return Optional.empty();
    }

    @Override
    public String snapshot() {
        return wrappers.stream().map(wrapper -> wrapper.getHolder().getIdentifier()).reduce((s, s2) -> s + ("\n" + s2)).orElse("no clients can be showed");
    }
}
