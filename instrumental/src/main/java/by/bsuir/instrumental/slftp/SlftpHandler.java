package by.bsuir.instrumental.slftp;

import by.bsuir.instrumental.packet.Packet;

public interface SlftpHandler {
    Packet handleRequest(Packet packet);
}
