package by.bsuir.instrumental.ftp.tftp.file.nack;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.tftp.file.block.table.BlockTable;
import lombok.Value;

import java.io.Serializable;

@Value
public class NackStructure implements Serializable {
    FileMetaData fileMetaData;
    BlockTable blockTable;
}
