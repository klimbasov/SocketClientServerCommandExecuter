package by.bsuir.instrumental.pool;

import lombok.Getter;
import lombok.Setter;

import java.net.SocketAddress;
import java.util.HashMap;

import static java.util.Objects.nonNull;

public class UuidAddressTable {
    private final HashMap<String, SocketAddress> addressTable;
    @Getter
    @Setter
    private SocketAddress defaultGateway;

    public UuidAddressTable() {
        this.addressTable = new HashMap<>();
    }

    public SocketAddress get(String uuid) {
        SocketAddress address = addressTable.get(uuid);
        return nonNull(address) ? address : getDefaultGateway();
    }

    public void put(String uuid, SocketAddress address) {
        addressTable.put(uuid, address);
    }

    public boolean contains(String uuid) {
        return addressTable.containsKey(uuid);
    }
}
