package by.bsuir.instrumental.pool.impl;

import by.bsuir.instrumental.service.MetaDataRecord;
import by.bsuir.instrumental.pool.SearchablePool;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MetaDataRecordPool implements SearchablePool<String, MetaDataRecord> {
    private final Map<String, MetaDataRecord> stringMetaDataRecordMap = new HashMap<>();
    @Override
    public void offer(MetaDataRecord obj) {
        stringMetaDataRecordMap.put(obj.getTargetId(), obj);
    }

    @Override
    public Optional<MetaDataRecord> poll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return stringMetaDataRecordMap.isEmpty();
    }

    @Override
    public Optional<MetaDataRecord> find(String id) {
        return Optional.ofNullable(stringMetaDataRecordMap.get(id));
    }
}
