package by.bsuir.instrumental.ftp.tftp.pool;

import by.bsuir.instrumental.ftp.tftp.file.input.FileInputStructure;
import by.bsuir.instrumental.pool.SearchableQueuePool;

import java.util.HashMap;
import java.util.Optional;

public class FileOutputPool implements SearchableQueuePool<String, FileInputStructure> {
    private final HashMap<String, FileInputStructure> stringFileInputStructureHashMap = new HashMap<>();

    @Override
    public void offer(FileInputStructure obj) {
        stringFileInputStructureHashMap.put(obj.getId(), obj);
    }

    @Override
    public Optional<FileInputStructure> poll() {
        return Optional.empty();
    }

    @Override
    public boolean isEmpty() {
        return stringFileInputStructureHashMap.isEmpty();
    }

    @Override
    public Optional<FileInputStructure> find(String id) {
        return Optional.of(stringFileInputStructureHashMap.get(id));
    }

    @Override
    public Optional<FileInputStructure> remove(String id) {
        return Optional.of(stringFileInputStructureHashMap.remove(id));
    }
}
