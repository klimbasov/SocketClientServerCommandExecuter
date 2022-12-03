package by.bsuir.instrumental.ftp.slftp.packet.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SlftpPacketType {
    PORTION((short) 0b100001),
    PORTION_REQ((short) 0b100010),
    GREETING((short) 0b100011),
    ABORT((short) 0b100100),
    DECLINE((short) 0b100101),
    NOT_PASSED((short) 0b100110),
    GREETING_REQ((short) 0b100111),
    UNDEFINED((short) 0b0);

    @Getter
    public final short typeId;

    public static SlftpPacketType getByTypeId(short typeId) {
        SlftpPacketType resultType = UNDEFINED;
        for (SlftpPacketType type : SlftpPacketType.values()) {
            if (type.typeId == typeId) {
                resultType = type;
                break;
            }
        }
        return resultType;
    }
}
