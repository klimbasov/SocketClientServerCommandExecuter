package by.bsuir.instrumental.slftp.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PortionRequest implements Serializable {
    String fileUri;
    String hostId;
    long portion;
}
