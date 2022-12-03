package by.bsuir.instrumental.ftp.tftp.file.block.table.portion;

import lombok.Value;

@Value
public class Portion {
    String id;
    byte[] content;
    int portionNum;
}
