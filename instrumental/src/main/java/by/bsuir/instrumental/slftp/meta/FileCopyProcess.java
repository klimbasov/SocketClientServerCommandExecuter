package by.bsuir.instrumental.slftp.meta;

import by.bsuir.instrumental.slftp.dto.FileMetaData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Data
@Accessors(chain = true)
public class FileCopyProcess implements Closeable {
    FileMetaData metaData;
    short timesDeclined;
    long portion;
    long portionsQuantity;
    FileOutputStream stream;

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
