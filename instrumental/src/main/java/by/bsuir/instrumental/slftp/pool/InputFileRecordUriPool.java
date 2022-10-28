package by.bsuir.instrumental.slftp.pool;

import by.bsuir.instrumental.pool.SearchablePool;
import by.bsuir.instrumental.slftp.meta.InputFileRecord;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class InputFileRecordUriPool implements SearchablePool<String, InputFileRecord> {
    private final Map<String, InputFileRecord> stringInputFileRecordHashMap = new HashMap<>();
    private final LinkedList<InputFileRecord> inputFileRecords = new LinkedList<>();
    @Override
    public void offer(InputFileRecord obj) {
        stringInputFileRecordHashMap.put(obj.getMetaData().getUrl(), obj);
        inputFileRecords.offer(obj);
    }

    @Override
    public Optional<InputFileRecord> poll() {
        Optional<InputFileRecord> optional = Optional.ofNullable(inputFileRecords.poll());
        optional.ifPresent(inputFileRecord -> stringInputFileRecordHashMap.remove(inputFileRecord.getMetaData().getUrl()));
        return optional;
    }

    @Override
    public boolean isEmpty() {
        return inputFileRecords.isEmpty();
    }

    @Override
    public Optional<InputFileRecord> find(String id) {
        return Optional.ofNullable(stringInputFileRecordHashMap.get(id));
    }

    @Override
    public Optional<InputFileRecord> remove(String id) {
        InputFileRecord inputFileRecord = stringInputFileRecordHashMap.remove(id);
        inputFileRecords.remove(inputFileRecord);
        return Optional.ofNullable(inputFileRecord);
    }
}
