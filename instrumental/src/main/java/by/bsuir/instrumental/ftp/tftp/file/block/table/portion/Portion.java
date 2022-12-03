package by.bsuir.instrumental.ftp.tftp.file.block.table.portion;

import lombok.Value;

import java.io.Serializable;

@Value
public class Portion implements Serializable {
    String id;
    byte[] content;
    int portionNum;
    long blockNum;
}
