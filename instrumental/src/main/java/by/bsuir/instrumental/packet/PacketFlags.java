package by.bsuir.instrumental.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PacketFlags {
    ACK((short) 0b1),
    ABORT((short) 0b10);
    @Getter
    public final short flagValue;
}
