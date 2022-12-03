package by.bsuir.instrumental.ftp.tftp.file.output;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.tftp.file.block.table.portion.Portion;
import by.bsuir.instrumental.ftp.tftp.file.block.table.BlockTable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static by.bsuir.instrumental.ftp.util.file.FileBlockIOUtil.generateBlockTable;

@Getter
public class FileOutputStructure {
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

    public FileOutputStructure(String id, FileMetaData fileMetaData, String path, long blockAmount, long portionAmount){
        this.id = id;
        this.path = path;
        this.blockAmount = blockAmount;
        this.metadata = fileMetaData;
        this.portionAmount = portionAmount;
        this.faultCounter = MAX_FAULT_COUNTER;
        this.blockNum = 0;
        this.blockTable = generateBlockTable(blockNum, portionAmount);
        this.portions = new ArrayList<>(blockTable.getBlockSize());
        this.startMils = System.currentTimeMillis();
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
            ++blockNum;
        }
        if(blockNum < blockAmount){
            this.blockTable = generateBlockTable(blockNum, portionAmount);
            this.portions = new ArrayList<>(blockTable.getBlockSize());
        }
    }

    public boolean isComplete(){
        return blockNum >= blockAmount;
    }

}
