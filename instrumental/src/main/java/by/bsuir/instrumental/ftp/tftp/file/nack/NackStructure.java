package by.bsuir.instrumental.ftp.tftp.file.nack;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.tftp.file.block.table.BlockTable;
import lombok.Value;

@Value
public class NackStructure {
    FileMetaData fileMetaData;
    BlockTable blockTable;
}
