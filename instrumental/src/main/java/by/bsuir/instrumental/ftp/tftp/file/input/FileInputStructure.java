package by.bsuir.instrumental.ftp.tftp.file.input;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.tftp.file.block.table.BlockTable;
import by.bsuir.instrumental.ftp.tftp.file.block.table.portion.Portion;
import by.bsuir.instrumental.ftp.util.file.FileBlockIOUtil;
import lombok.Getter;

import java.util.List;
@Getter
public class FileInputStructure {
    private final String id;
    private final FileMetaData metadata;
    private final String path;
    private long blockNum;
    private final long blockAmount;
    private List<Portion> block;

    public FileInputStructure(String id, FileMetaData fileMetaData, String path, long blockAmount){
        this.id = id;
        this.path = path;
        this.blockAmount = blockAmount;
        this.metadata = fileMetaData;
        block = FileBlockIOUtil.readBlock(path, id, blockNum);
    }


    public FileInputStructure(String id, FileMetaData fileMetaData, String path, long blockAmount, BlockTable blockTable){
        this(id, fileMetaData, path, blockAmount);
        loadForBlockTable(blockTable);
    }

    private void loadForBlockTable(BlockTable blockTable){
        blockNum = blockTable.getBlockId();
        block = FileBlockIOUtil.readBlock(path, id, blockNum);
    }

    public void nextBlock(){
        if(blockNum < blockAmount){
            ++blockNum;
            block = FileBlockIOUtil.readBlock(path, id, blockNum);
        }
    }

    public boolean isComplete(){
        return blockNum >= blockAmount;
    }
}
