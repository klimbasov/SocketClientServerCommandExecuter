package by.bsuir.instrumental.ftp.tftp.file.ack;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import lombok.Value;

@Value
public class AckStructure {
    FileMetaData metaData;
    long blockNum;
}
