package by.bsuir.instrumental.pool;

import by.bsuir.instrumental.node.AbstractNodeIOWrapper;

import java.util.Optional;

public interface SearchableSocketIOWrapperPool {
    Optional<AbstractNodeIOWrapper> find(String id);
}
