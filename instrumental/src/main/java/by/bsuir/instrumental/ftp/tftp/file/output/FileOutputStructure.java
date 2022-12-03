package by.bsuir.instrumental.ftp.tftp.file.output;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.tftp.file.block.table.portion.Portion;
import by.bsuir.instrumental.ftp.tftp.file.block.table.BlockTable;
import lombok.Data;

import java.util.List;

@Data
public class FileOutputStructure {
    private String id;
    private final FileMetaData metadata;
    private final long blockAmount;
    private final long portionAmount;
    private final String path;
    private BlockTable blockTable;
    private long blockNum;
    private List<Portion> portions;
}
