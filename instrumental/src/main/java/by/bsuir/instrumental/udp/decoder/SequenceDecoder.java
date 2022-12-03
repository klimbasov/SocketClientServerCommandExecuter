package by.bsuir.instrumental.udp.decoder;

import java.net.DatagramPacket;

public interface SequenceDecoder {
    byte[] decode(DatagramPacket[] input);
}
