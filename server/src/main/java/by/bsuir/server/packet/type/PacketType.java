package by.bsuir.server.packet.type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PacketType {
    DATA_PACKAGE((short) 1),
    COMMAND_PACKAGE((short) 2),
    INFORM_PACKAGE((short) 3);

    public final short type;
}