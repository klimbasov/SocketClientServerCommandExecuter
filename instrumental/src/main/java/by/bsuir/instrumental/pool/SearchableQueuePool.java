package by.bsuir.instrumental.pool;

import java.util.Optional;

public interface SearchableQueuePool<K, T> extends QueuePool<T> {
    Optional<T> find(K id);

    Optional<T> remove(K id);
}
