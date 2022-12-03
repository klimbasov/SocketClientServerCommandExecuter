package by.bsuir.instrumental.ftp.tftp.file.block.table;

import lombok.Data;

@Data
public class BlockTable {
    private byte[] table;
    private int blockSize;
    private long blockId;
}
