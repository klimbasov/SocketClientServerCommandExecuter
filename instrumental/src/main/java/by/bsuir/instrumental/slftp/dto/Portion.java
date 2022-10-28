package by.bsuir.instrumental.slftp.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Portion implements Serializable {
    byte[] content;
    long portionNum;
    String hostId;
    String fileUri;
    long actualSize;
}
