package by.bsuir.instrumental.slftp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FileMetaData implements Serializable {
    String url;
    String hostId;
    long size;
}
