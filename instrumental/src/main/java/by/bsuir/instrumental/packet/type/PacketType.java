package by.bsuir.instrumental.packet.type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PacketType {
    DATA_PACKAGE((short) 1),
    COMMAND_PACKAGE((short) 2),
    INFORM_PACKAGE((short) 3);

    public final short type;

    public static PacketType getInstance(short typeId){
        PacketType instance = null;
        for (PacketType packetType : PacketType.values()){
            if(packetType.type == typeId){
                instance = packetType;
            }
        }
        return instance;
    }
}