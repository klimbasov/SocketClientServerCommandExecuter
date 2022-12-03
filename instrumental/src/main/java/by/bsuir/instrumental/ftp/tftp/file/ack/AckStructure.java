package by.bsuir.instrumental.ftp.tftp.file.ack;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import lombok.Value;

import java.io.Serializable;

@Value
public class AckStructure implements Serializable {
    FileMetaData metaData;
    long blockNum;
}
