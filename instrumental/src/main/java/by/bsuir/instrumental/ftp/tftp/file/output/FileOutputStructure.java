package by.bsuir.instrumental.ftp.tftp.file.output;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.tftp.file.block.table.portion.Portion;
import by.bsuir.instrumental.ftp.tftp.file.block.table.BlockTable;
import by.bsuir.instrumental.ftp.util.file.FileBlockIOUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static by.bsuir.instrumental.ftp.util.file.FileBlockIOUtil.generateBlockTable;

@Getter
@Slf4j
public class FileOutputStructure implements Closeable {
    private static final short MAX_FAULT_COUNTER = 5;
    private final String id;
    private final FileMetaData metadata;
    private final long blockAmount;
    private final long portionAmount;
    private final String path;
    private BlockTable blockTable;
    private long blockNum;
    private List<Portion> portions;
    private short faultCounter;
    private final long startMils;
    private BufferedOutputStream bos;

    //testing fields
    private long portionsReceived = 0;
    private long nacksSent = 0;
    private long acksSent = 0;

    public FileOutputStructure(String id, FileMetaData fileMetaData, String path, long blockAmount, long portionAmount){
        this.id = id;
        this.path = path;
        this.blockAmount = blockAmount;
        this.metadata = fileMetaData;
        this.portionAmount = portionAmount;
        this.faultCounter = MAX_FAULT_COUNTER;
        this.blockNum = 0;
        this.blockTable = generateBlockTable(blockNum, portionAmount);
        this.portions = new ArrayList<>(Collections.nCopies(blockTable.getBlockSize(), null));
        this.startMils = System.currentTimeMillis();
        this.bos = FileBlockIOUtil.getOStream(path);
    }

    public void faultOccurred(){
        --faultCounter;
    }

    public boolean isFaultOverhead(){
        return faultCounter == 0;
    }

    public void renewFaultCounter(){
        faultCounter = MAX_FAULT_COUNTER;
    }

    public void incBlockNum(){
        if(blockNum < blockAmount){
            FileBlockIOUtil.writeBlock(this);
            ++blockNum;
        }
        if(blockNum < blockAmount){
            this.blockTable = generateBlockTable(blockNum, portionAmount);
            this.portions = new ArrayList<>(Collections.nCopies(blockTable.getBlockSize(), null));
        }
    }

    public boolean isComplete(){
        return blockNum >= blockAmount;
    }

    public void incPortionsRes(){
        ++portionsReceived;
    }
    public void incAck(){
        ++acksSent;
    }
    public void incNack(){
        ++nacksSent;
    }

    @Override
    public void close() {
        try {
            bos.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
