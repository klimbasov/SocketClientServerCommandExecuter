package by.bsuir.instrumental.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PacketFlags {
    ACK(0b1),
    ABORT(0b10);
    @Getter
    public final int flagValue;
}
