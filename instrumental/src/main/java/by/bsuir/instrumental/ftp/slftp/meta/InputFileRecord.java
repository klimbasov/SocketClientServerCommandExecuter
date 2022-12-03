package by.bsuir.instrumental.ftp.slftp.meta;

import by.bsuir.instrumental.ftp.slftp.dto.FileMetaData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;

@Data
@Accessors(chain = true)
public class InputFileRecord implements Closeable {
    FileMetaData metaData;
    FileInputStream stream;

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
