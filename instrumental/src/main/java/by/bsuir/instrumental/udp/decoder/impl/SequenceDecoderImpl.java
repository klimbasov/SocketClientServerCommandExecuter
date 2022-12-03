package by.bsuir.instrumental.udp.decoder.impl;

import by.bsuir.instrumental.udp.decoder.SequenceDecoder;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;

public class SequenceDecoderImpl implements SequenceDecoder {
    private ByteArrayOutputStream stream;
    @Override
    public byte[] decode(DatagramPacket[] input) {
//        stream.write();
//        return input.getData();
        return null;
    }
}
