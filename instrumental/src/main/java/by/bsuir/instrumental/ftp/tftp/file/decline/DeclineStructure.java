package by.bsuir.instrumental.ftp.tftp.file.decline;

import lombok.Value;

import java.io.Serializable;

@Value
public class DeclineStructure implements Serializable {
    String code;
    String message;
}
