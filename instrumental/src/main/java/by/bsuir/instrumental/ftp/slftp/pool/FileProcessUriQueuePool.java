package by.bsuir.instrumental.ftp.slftp.pool;

import by.bsuir.instrumental.pool.SearchableQueuePool;
import by.bsuir.instrumental.ftp.slftp.meta.FileCopyProcess;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class FileProcessUriQueuePool implements SearchableQueuePool<String, FileCopyProcess> {
    private final Map<String, FileCopyProcess> fileOutputStreamMap = new WeakHashMap<>();
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
}
