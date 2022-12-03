package by.bsuir.instrumental.ftp.tftp.file.input;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.tftp.file.block.table.portion.Portion;
import lombok.Data;

import java.util.List;

@Data
public class FileInputStructure {
    private String id;
    private final FileMetaData metadata;
    private final String path;
    private long blockNum;
    private final long blockAmount;
    private List<Portion> block;
}
