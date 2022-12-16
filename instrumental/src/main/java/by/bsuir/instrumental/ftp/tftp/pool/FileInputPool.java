package by.bsuir.instrumental.ftp.tftp.pool;

import by.bsuir.instrumental.ftp.tftp.file.input.FileInputStructure;
import by.bsuir.instrumental.pool.SearchableQueuePool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

public class FileInputPool implements SearchableQueuePool<String, FileInputStructure> {
    private final HashMap<String, FileInputStructure> stringFileInputStructureHashMap = new HashMap<>();
    private final LinkedList<FileInputStructure> fileInputStructureLinkedList = new LinkedList<>();

    @Override
    public void offer(FileInputStructure obj) {
        stringFileInputStructureHashMap.put(obj.getId(), obj);
        fileInputStructureLinkedList.offer(obj);
    }

    @Override
    public Optional<FileInputStructure> poll() {
        Optional<FileInputStructure> optional = Optional.ofNullable(fileInputStructureLinkedList.poll());
        optional.ifPresent(fileInputStructureLinkedList::offer);
        return optional;
    }

    @Override
    public boolean isEmpty() {
        return stringFileInputStructureHashMap.isEmpty();
    }

    @Override
    public Optional<FileInputStructure> find(String id) {
        return Optional.ofNullable(stringFileInputStructureHashMap.get(id));
    }

    @Override
    public Optional<FileInputStructure> remove(String id) {
        Optional<FileInputStructure> optional = Optional.ofNullable(stringFileInputStructureHashMap.remove(id));
        optional.ifPresent(fileInputStructureLinkedList::remove);
        return optional;
    }
}
