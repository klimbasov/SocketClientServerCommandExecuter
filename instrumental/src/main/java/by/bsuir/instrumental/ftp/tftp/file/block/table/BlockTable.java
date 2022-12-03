package by.bsuir.instrumental.ftp.tftp.file.block.table;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class BlockTable implements Serializable {
    private byte[] table;
    private final int blockSize;
    private final long blockId;

    public BlockTable(int blockSize, long blockId){
        this.blockId = blockId;
        this.blockSize = blockSize;
        init();
    }

    private void init() {
        int tableElements = Math.ceilDiv(blockSize, 8);
        int lastBlockElement = blockSize & 0x7;
        lastBlockElement = (lastBlockElement == 0) ? 0x7 : lastBlockElement-1;
        table = new byte[tableElements];
        for(int counter = 0; counter < tableElements - 1; counter++){
            table[counter] = (byte) 0xff;
        }
        table[tableElements-1] = (byte) ((2 << lastBlockElement) - 1);
    }

    public boolean isComplete() {
        boolean isFull = true;
        for (byte b : table) {
            if (b != 0) {
                isFull = false;
                break;
            }
        }
        return isFull;
    }
}
