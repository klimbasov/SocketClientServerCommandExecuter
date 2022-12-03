package by.bsuir.instrumental.ftp.tftp.file.abort;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import lombok.Value;

@Value
public class AbortStructure {
    FileMetaData fileMetaData;
    String code;
    String message;
}
