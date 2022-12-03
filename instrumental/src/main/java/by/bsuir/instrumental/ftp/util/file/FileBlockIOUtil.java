package by.bsuir.instrumental.ftp.util.file;

import by.bsuir.instrumental.ftp.tftp.file.block.table.portion.Portion;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileBlockIOUtil {
    private static final int PORTION_SIZE = 1024 << 1;
    private static final int MAX_BLOCK_SIZE = 128;

    private static final int BLOCK_SIZE_IN_BYTES = PORTION_SIZE * MAX_BLOCK_SIZE;

    public static List<Portion> readBlock(String path, long blockNum){
        long offset = blockNum * MAX_BLOCK_SIZE * PORTION_SIZE;
        File file = new File(path);
        List<Portion> block;
        throwIf(!file.exists(), "Requested file does not exist");
        throwIf(offset > file.length(), "Offset length has bigger value than file length");
        try(FileInputStream fis = new FileInputStream(file)){
            fis.getChannel().position(offset);
            byte[] data = fis.readNBytes(BLOCK_SIZE_IN_BYTES);
            block = rawDataToBlock(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return block;
    }

    public static void writeBlock(String path, long blockNum, List<Portion> block) {
        long offset = blockNum * BLOCK_SIZE_IN_BYTES;
        File file = new File(path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try(FileOutputStream fos = new FileOutputStream(file)){
            fos.getChannel().position(offset);
            for(int counter = 0; counter < block.size(); counter++){
                fos.write(block.get(counter).getContent());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Portion> rawDataToBlock(byte[] data) {
        int portionsAmount = Math.ceilDiv(data.length, BLOCK_SIZE_IN_BYTES);
        List<Portion> block = new ArrayList<>(portionsAmount);
        int offset = 0;
        int lastPortionSize = data.length % BLOCK_SIZE_IN_BYTES;
        int fullPortionsAmount = (lastPortionSize == 0) ? (portionsAmount) : (portionsAmount -1);
        for (int counter = 0; counter < fullPortionsAmount; counter ++){
            Portion portion = new Portion()
                    .setPortionNum(counter)
                    .setContent(Arrays.copyOfRange(data, offset, offset + BLOCK_SIZE_IN_BYTES));
            block.add(portion);
            offset += BLOCK_SIZE_IN_BYTES;
        }
        if(lastPortionSize != 0){
            Portion portion = new Portion()
                    .setPortionNum(portionsAmount - 1)
                    .setContent(Arrays.copyOfRange(data, offset, offset + lastPortionSize));
            block.add(portion);
        }
        return block;
    }

    private static void throwIf(boolean file, String Requested_file_does_not_exist) {
        if(file){
            throw new RuntimeException(Requested_file_does_not_exist);
        }
    }
}
