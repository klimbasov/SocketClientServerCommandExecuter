package by.bsuir.instrumental.ftp.tftp.file.abort;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import lombok.Value;

import java.io.Serializable;

@Value
public class AbortStructure implements Serializable {
    FileMetaData fileMetaData;
    String code;
    String message;
}
