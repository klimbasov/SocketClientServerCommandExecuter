package by.bsuir.instrumental.udp.encoder;

import java.net.DatagramPacket;

public interface SequenceEncoder {
    DatagramPacket[] encode(byte[] input);
}
