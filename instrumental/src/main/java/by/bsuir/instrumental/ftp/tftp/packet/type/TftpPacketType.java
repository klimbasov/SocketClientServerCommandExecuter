package by.bsuir.instrumental.ftp.tftp.packet.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TftpPacketType {
    METADATA((short) 0b100001),
    ACK((short) 0b100010),
    NACK((short) 0b101000),
    PORTION((short) 0b101001),
    GREETING((short) 0b100011),
    ABORT((short) 0b100100),
    DECLINE((short) 0b100101),
    NOT_PASSED((short) 0b100110),
    GREETING_REQ((short) 0b100111),
    COMPLETE((short) 0b101010),
    UNDEFINED((short) 0b0);

    @Getter
    public final short typeId;

    public static TftpPacketType getByTypeId(short typeId) {
        TftpPacketType resultType = UNDEFINED;
        for (TftpPacketType type : TftpPacketType.values()) {
            if (type.typeId == typeId) {
                resultType = type;
                break;
            }
        }
        return resultType;
    }
}
