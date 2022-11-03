package by.bsuir.instrumental.pool;

import java.util.Optional;

public interface QueuePool<T> {
    void offer(T obj);

    Optional<T> poll();

    boolean isEmpty();
}
