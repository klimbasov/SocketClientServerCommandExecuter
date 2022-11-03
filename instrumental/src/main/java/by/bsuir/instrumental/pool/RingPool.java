package by.bsuir.instrumental.pool;

import java.util.Optional;

public interface RingPool<T>{
    void offer(T obj);
    Optional<T> getNext();
    boolean isEmpty();
}
