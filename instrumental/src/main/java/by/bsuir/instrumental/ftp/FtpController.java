package by.bsuir.instrumental.ftp;

import by.bsuir.instrumental.packet.Packet;

import java.io.Closeable;
import java.util.List;

public interface FtpController extends Closeable {
    List<Packet> receive();

    void send(Packet response);

    void download(String path, String sourceId);

    void upload(String path, String destinationId);

    void close();
}
