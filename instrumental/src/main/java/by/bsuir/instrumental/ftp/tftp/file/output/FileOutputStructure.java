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
import static by.bsuir.instrumental.ftp.util.file.ftp.FtpTimingUtil.getDelayFuncVal;

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
    private long nackCounter;
    private long lastDelayMils;

    public FileOutputStructure(String id, FileMetaData fileMetaData, String path, long blockAmount, long portionAmount){
        this.id = id;
        this.path = path;
        this.blockAmount = blockAmount;
        this.metadata = fileMetaData;
        this.portionAmount = portionAmount;
        this.faultCounter = MAX_FAULT_COUNTER;
        this.blockNum = 0;
        this.nackCounter = 0;
        this.blockTable = generateBlockTable(blockNum, portionAmount);
        this.portions = new ArrayList<>(Collections.nCopies(blockTable.getBlockSize(), null));
        this.startMils = System.currentTimeMillis();
        this.bos = FileBlockIOUtil.getOStream(path);
        this.lastDelayMils = System.currentTimeMillis();
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

    public boolean isNackNeeded(){
        boolean isNeeded = false;
        long curMils = System.currentTimeMillis();
        if(lastDelayMils < curMils){
            isNeeded = true;
            ++nackCounter;
            lastDelayMils = curMils + getDelayFuncVal(nackCounter);
        }
        return isNeeded;
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
    public void incNackCounter(){
        lastDelayMils = System.currentTimeMillis();
        ++nackCounter;
    }

    public void dropNackCounter(){
        nackCounter = 0;
        lastDelayMils = System.currentTimeMillis();
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
