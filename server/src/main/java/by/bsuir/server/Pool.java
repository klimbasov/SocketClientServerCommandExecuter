package by.bsuir.server;

import java.util.Optional;

public interface Pool<T> {
    void offer(T obj);

    Optional<T> poll();

    boolean isEmpty();
}
