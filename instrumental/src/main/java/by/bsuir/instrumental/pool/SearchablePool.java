package by.bsuir.instrumental.pool;

import by.bsuir.instrumental.node.AbstractNodeIOWrapper;

import java.util.Optional;

public interface SearchablePool<K, T> extends Pool<T> {
    Optional<T> find(K id);

    Optional<T> remove(K id);
}
