package by.bsuir.client.task.impl;

import by.bsuir.instrumental.command.service.MetaDataRecord;
import by.bsuir.instrumental.pool.SearchablePool;
import by.bsuir.instrumental.task.Task;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class FileDownloadingResumeTaskImpl implements Task {
    private final SearchablePool<String, MetaDataRecord> stringMetaDataRecordSearchablePool;

    @Override
    public void run() {

    }
}
