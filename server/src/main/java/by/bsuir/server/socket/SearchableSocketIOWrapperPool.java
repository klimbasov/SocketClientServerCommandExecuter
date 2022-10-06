package by.bsuir.server.socket;

import java.util.Optional;

public interface SearchableSocketIOWrapperPool {
    Optional<SocketIOWrapper> find(String id);
}
