package by.bsuir.instrumental.slftp.pool;

import by.bsuir.instrumental.pool.SearchablePool;
import by.bsuir.instrumental.slftp.meta.FileCopyProcess;

import java.util.*;

public class FileProcessUriPool implements SearchablePool<String, FileCopyProcess> {
    private final Map<String, FileCopyProcess> fileOutputStreamMap = new HashMap<>();
    private final LinkedList<FileCopyProcess> fileCopyProcesses = new LinkedList<>();
    @Override
    public void offer(FileCopyProcess obj) {
        fileOutputStreamMap.put(obj.getMetaData().getUrl(), obj);
        fileCopyProcesses.offer(obj);
    }

    @Override
    public Optional<FileCopyProcess> poll() {
        Optional<FileCopyProcess> optionalFileCopyProcess = Optional.ofNullable(fileCopyProcesses.poll());
        optionalFileCopyProcess.ifPresent(fileCopyProcess -> fileOutputStreamMap.remove(fileCopyProcess.getMetaData().getUrl()));
        return optionalFileCopyProcess;
    }

    @Override
    public boolean isEmpty() {
        return fileCopyProcesses.isEmpty();
    }

    @Override
    public Optional<FileCopyProcess> find(String id) {
        return Optional.ofNullable(fileOutputStreamMap.get(id));
    }

    @Override
    public Optional<FileCopyProcess> remove(String id) {
        FileCopyProcess copyProcess = fileOutputStreamMap.remove(id);
        fileCopyProcesses.remove(copyProcess);
        return Optional.ofNullable(copyProcess);
    }

    public List<FileCopyProcess> getInternal(){
        return fileCopyProcesses;
    }
}
