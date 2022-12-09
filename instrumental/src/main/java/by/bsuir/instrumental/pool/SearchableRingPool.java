package by.bsuir.instrumental.pool;

import java.util.Optional;

public interface SearchableRingPool<K, T> {
    Optional<T> find(K id);

    void put(K key, T value);
}
