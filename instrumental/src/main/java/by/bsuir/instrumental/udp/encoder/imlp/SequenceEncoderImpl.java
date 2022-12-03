package by.bsuir.instrumental.udp.encoder.imlp;

import by.bsuir.instrumental.udp.encoder.SequenceEncoder;

import java.net.DatagramPacket;

public class SequenceEncoderImpl implements SequenceEncoder {

    @Override
    public DatagramPacket[] encode(byte[] input) {
        return new DatagramPacket[0];
    }
}
