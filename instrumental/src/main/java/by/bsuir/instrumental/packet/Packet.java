package by.bsuir.instrumental.packet;

import lombok.Data;
import lombok.ToString;
import lombok.Value;

import java.io.Serializable;

@Data
@ToString
@Value
public class Packet implements Serializable {
    private static final long SerialVersionUID = 1L;
    byte[] body;
    byte[] sourceId;
    byte[] targetId;
    short type;
    short flags;
}
