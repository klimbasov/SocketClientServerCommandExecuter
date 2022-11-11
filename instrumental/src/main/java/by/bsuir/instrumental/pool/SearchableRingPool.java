package by.bsuir.instrumental.pool;

import java.util.Optional;

public interface SearchableRingPool<K, T> extends RingPool<T> {
    Optional<T> find(K id);

    Optional<T> remove(K id);
}
