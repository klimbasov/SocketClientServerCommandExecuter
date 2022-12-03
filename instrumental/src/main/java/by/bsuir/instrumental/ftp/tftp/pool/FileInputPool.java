package by.bsuir.instrumental.ftp.tftp.pool;

import by.bsuir.instrumental.pool.SearchableQueuePool;
import by.bsuir.instrumental.ftp.tftp.file.output.FileOutputStructure;

import java.util.HashMap;
import java.util.Optional;

public class FileInputPool implements SearchableQueuePool<String, FileOutputStructure> {
    private final HashMap<String, FileOutputStructure> stringFileOutputStructureHashMap = new HashMap<>();

    @Override
    public void offer(FileOutputStructure obj) {
        stringFileOutputStructureHashMap.put(obj.getId(), obj);
    }

    @Override
    public Optional<FileOutputStructure> poll() {
        return Optional.empty();
    }

    @Override
    public boolean isEmpty() {
        return stringFileOutputStructureHashMap.isEmpty();
    }

    @Override
    public Optional<FileOutputStructure> find(String id) {
        return Optional.of(stringFileOutputStructureHashMap.get(id));
    }

    @Override
    public Optional<FileOutputStructure> remove(String id) {
        return Optional.of(stringFileOutputStructureHashMap.remove(id));
    }
}
