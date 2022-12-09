package by.bsuir.instrumental.ftp.tftp.file.input;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.tftp.file.block.table.BlockTable;
import by.bsuir.instrumental.ftp.tftp.file.block.table.portion.Portion;
import by.bsuir.instrumental.ftp.util.file.FileBlockIOUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

@Getter
@Slf4j
public class FileInputStructure implements Closeable {
    private final String id;
    private final FileMetaData metadata;
    private final String path;
    private long blockNum;
    private final long blockAmount;
    private List<Portion> block;
    private BufferedInputStream bis;

    //testing fields
    private long nacksReceived = 0;
    private long acksReceived = 0;
    private long portionsSent = 0;

    public FileInputStructure(String id, FileMetaData fileMetaData, String path, long blockAmount){
        this.id = id;
        this.path = path;
        this.blockAmount = blockAmount;
        this.metadata = fileMetaData;
        bis = FileBlockIOUtil.getIStream(path);
        block = FileBlockIOUtil.readBlock(this);
    }


    public FileInputStructure(String id, FileMetaData fileMetaData, String path, long blockAmount, BlockTable blockTable){
        this(id, fileMetaData, path, blockAmount);
        loadForBlockTable(blockTable);
    }

    private void loadForBlockTable(BlockTable blockTable){
        blockNum = blockTable.getBlockId();
        block = FileBlockIOUtil.readBlock(this);
    }

    public void nextBlock(){
        if(blockNum < blockAmount){
            ++blockNum;
            block = FileBlockIOUtil.readBlock(this);
        }
    }

    public boolean isComplete(){
        return blockNum >= blockAmount - 1;
    }

    public void incNack(){
        ++nacksReceived;
    }

    public void incAck(){
        ++acksReceived;
    }

    public void incPortionsSent(){
        ++portionsSent;
    }

    @Override
    public void close() {
        try {
            bis.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
