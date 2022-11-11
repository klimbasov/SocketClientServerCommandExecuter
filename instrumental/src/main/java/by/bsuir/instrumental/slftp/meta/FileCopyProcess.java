package by.bsuir.instrumental.slftp.meta;

import by.bsuir.instrumental.slftp.dto.FileMetaData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

@Data
@Accessors(chain = true)
public class FileCopyProcess implements Closeable {
    long mils;
    long lastTimeTransceive;
    FileMetaData metaData;
    short timesDeclined;
    long portion;
    long portionsQuantity;
    OutputStream stream;

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
