package by.bsuir.instrumental.packet;

import lombok.Data;
import lombok.ToString;
import lombok.Value;

import java.io.Serializable;

@Data
@ToString
@Value
public class Packet implements Serializable {
    byte[] body;
    byte[] sourceId;
    byte[] targetId;
    short type;
    short flags;
}
