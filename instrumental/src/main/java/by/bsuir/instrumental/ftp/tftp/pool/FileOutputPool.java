package by.bsuir.instrumental.ftp.tftp.pool;

import by.bsuir.instrumental.pool.SearchableQueuePool;
import by.bsuir.instrumental.ftp.tftp.file.output.FileOutputStructure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class FileOutputPool implements SearchableQueuePool<String, FileOutputStructure> {
    private final HashMap<String, FileOutputStructure> stringFileOutputStructureHashMap = new HashMap<>();
    private final LinkedList<FileOutputStructure> fileOutputStructureLinkedList = new LinkedList<>();

    @Override
    public void offer(FileOutputStructure obj) {
        if(!stringFileOutputStructureHashMap.containsKey(obj.getId())){
            fileOutputStructureLinkedList.offer(obj);
            stringFileOutputStructureHashMap.put(obj.getId(), obj);
        }
    }

    @Override
    public Optional<FileOutputStructure> poll() {
        Optional<FileOutputStructure> optional = Optional.ofNullable(fileOutputStructureLinkedList.poll());
        optional.ifPresent(fileOutputStructureLinkedList::offer);
        return optional;
    }

    @Override
    public boolean isEmpty() {
        return stringFileOutputStructureHashMap.isEmpty();
    }

    @Override
    public Optional<FileOutputStructure> find(String id) {
        return Optional.ofNullable(stringFileOutputStructureHashMap.get(id));
    }

    @Override
    public Optional<FileOutputStructure> remove(String id) {
        Optional<FileOutputStructure> optional = Optional.ofNullable(stringFileOutputStructureHashMap.remove(id));
        optional.ifPresent(fileOutputStructureLinkedList::remove);
        return optional;
    }

    public List<FileOutputStructure> getAllDelayed(){
        return fileOutputStructureLinkedList.stream().filter(FileOutputStructure::isNackNeeded).toList();
    }
}