package by.bsuir.instrumental.ftp.util.file;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import by.bsuir.instrumental.ftp.tftp.file.block.table.BlockTable;
import by.bsuir.instrumental.ftp.tftp.file.block.table.portion.Portion;
import by.bsuir.instrumental.ftp.tftp.file.input.FileInputStructure;
import by.bsuir.instrumental.ftp.tftp.file.output.FileOutputStructure;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FileBlockIOUtil {
    public static final int PORTION_SIZE = 1024 << 1;
    public static final int MAX_BLOCK_SIZE = 2 << 6;//64

    public static final int BLOCK_SIZE_IN_BYTES = PORTION_SIZE * MAX_BLOCK_SIZE;
    private static final int BUFFER_SIZE = 1024 << 6;

    public static List<Portion> readBlock(FileInputStructure structure){
        long blockNum = structure.getBlockNum();
        String fileId = structure.getId();
        List<Portion> block;
        BufferedInputStream inputStream = structure.getBis();
        try{
            byte[] data = inputStream.readNBytes(BLOCK_SIZE_IN_BYTES);
            block = rawDataToBlock(data, fileId, blockNum);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return block;
    }

    public static void writeBlock(FileOutputStructure structure) {
        try{
            for (Portion portion : structure.getPortions()) {
                structure.getBos().write(portion.getContent());
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

//    public static void writeBlock(String path, long blockNum, List<Portion> block) {
//        long offset = blockNum * BLOCK_SIZE_IN_BYTES;
//        File file = new File(path);
//        if(!file.exists()){
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        try(FileOutputStream fos = new FileOutputStream(file)){
//            fos.getChannel().position(offset);
//            for (Portion portion : block) {
//                fos.write(portion.getContent());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static long getBlockAmount(FileMetaData metaData) {
        return Math.ceilDiv(metaData.getSize(), BLOCK_SIZE_IN_BYTES);
    }

    public static Optional<FileMetaData> getMetadataByFilePath(String url, String hostId) {
        Optional<FileMetaData> optional = Optional.empty();
        File file = new File(url);
        if (file.exists() && !file.isDirectory()) {
            FileMetaData metaData = new FileMetaData()
                    .setUrl(url)
                    .setSize(file.length())
                    .setHostId(hostId);
            optional = Optional.of(metaData);
        }
        return optional;
    }

    public static BlockTable generateBlockTable(long blockNum, long portionAmount){
        long offset = blockNum * MAX_BLOCK_SIZE;
        long portionsRemaining = portionAmount - offset;
        int blockSize = portionsRemaining < MAX_BLOCK_SIZE ? (int) portionsRemaining : MAX_BLOCK_SIZE;

        return new BlockTable(blockSize, blockNum);
    }

    private static List<Portion> rawDataToBlock(byte[] data, String fileId, long blockNum) {
        int portionsAmount = Math.ceilDiv(data.length, PORTION_SIZE);
        List<Portion> block = new ArrayList<>(portionsAmount);
        int offset = 0;
        int lastPortionSize = data.length % PORTION_SIZE;
        int fullPortionsAmount = (lastPortionSize == 0) ? (portionsAmount) : (portionsAmount -1);
        for (int counter = 0; counter < fullPortionsAmount; counter ++){
            byte[] content = Arrays.copyOfRange(data, offset, offset + PORTION_SIZE);
            Portion portion = new Portion(fileId, content, counter, blockNum);
            block.add(portion);
            offset += PORTION_SIZE;
        }
        if(lastPortionSize != 0){
            byte[] content = Arrays.copyOfRange(data, offset, offset + lastPortionSize);
            Portion portion = new Portion(fileId, content, portionsAmount - 1, blockNum);
            block.add(portion);
        }
        return block;
    }

    private static void throwIf(boolean file, String Requested_file_does_not_exist) {
        if(file){
            throw new RuntimeException(Requested_file_does_not_exist);
        }
    }

    public static void normalizePath(String path) {
        File file = new File(path);
        file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedOutputStream getOStream(String path) {
        File file = new File(path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return new BufferedOutputStream(new FileOutputStream(file, true), BUFFER_SIZE);//todo implement fertilisation for files
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedInputStream getIStream(String path) {
        File file = new File(path);
        if(!file.exists()){
            throw new RuntimeException("No such file exist");
        }
        try {
            return new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);//todo implement fertilisation for files
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
