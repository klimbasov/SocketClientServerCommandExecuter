package by.bsuir.server.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PacketFlags {
    ACK(0b1),
    ABORT(0b10);
    @Getter
    private final int flagValue;
}
