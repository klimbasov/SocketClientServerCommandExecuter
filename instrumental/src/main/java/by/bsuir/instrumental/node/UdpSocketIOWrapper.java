package by.bsuir.instrumental.node;

import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.pool.UuidAddressTable;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.*;

import static java.util.Objects.nonNull;

@Slf4j
public class UdpSocketIOWrapper extends AbstractNodeIOWrapper {
    private static final Packet FAULT_ADDRESS_NOT_FOUND_PACKET = new Packet("no root found for request".getBytes(), "".getBytes(),
            "".getBytes(), PacketType.INFORM_PACKAGE.typeId, PacketFlags.ACK.flagValue);
    private static final int DATAGRAM_PACKET_SIZE = 1024 << 2;
    private final Queue<Packet> callbackQueue;
    private final Queue<Packet> packetQueue;
    private final UuidAddressTable addressTable;
    private DatagramSocket socket;
    private boolean isClosed;

    public UdpSocketIOWrapper(UuidAddressTable addressTable) {
        super(null);
        this.callbackQueue = new LinkedList<>();
        this.packetQueue = new LinkedList<>();
        this.addressTable = addressTable;
        this.isClosed = true;
    }

    @Override
    public List<Packet> receive() {
        List<Packet> packets;
        if (callbackQueue.isEmpty()) {
            receiveFromSocket();
            packets = new ArrayList<>(packetQueue);
            packetQueue.clear();
        } else {
            packets = new ArrayList<>(callbackQueue);
            callbackQueue.clear();
        }
        return packets;
    }

    @Override
    public void send(List<Packet> packets) {
        packets.forEach(this::sendPacketHandler);
    }

    private void sendPacketHandler(Packet packet) {
        String uuid = new String(packet.getTargetId());
        SocketAddress address = addressTable.get(uuid);
        if (nonNull(address)) {
            sendToSocket(packet, address);
        } else {
            callbackQueue.add(FAULT_ADDRESS_NOT_FOUND_PACKET);
        }
    }

    private void sendToSocket(Packet response, SocketAddress address) {
        DatagramPacket packet;
        byte[] data;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(response);
            data = Arrays.copyOf(byteArrayOutputStream.toByteArray(), DATAGRAM_PACKET_SIZE);
            packet = new DatagramPacket(data, DATAGRAM_PACKET_SIZE);
            packet.setSocketAddress(address);
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveFromSocket() {
        byte[] data = new byte[DATAGRAM_PACKET_SIZE];
        DatagramPacket datagramPacket = new DatagramPacket(data, DATAGRAM_PACKET_SIZE);
        try {
            while (true) {
                socket.receive(datagramPacket);
                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());
                     ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                    Packet packet = (Packet) objectInputStream.readObject();
                    packetQueue.offer(packet);
                    String source = new String(packet.getSourceId());
                    SocketAddress address = datagramPacket.getSocketAddress();
                    if (!address.equals(addressTable.get(source))) {
                        addressTable.put(source, address);
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("received packet was corrupted");
                }
            }
        } catch (SocketTimeoutException ignored) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAvailable() {
        return !isClosed;
    }

    @Override
    public void close() throws Exception {
        if (socket != null) {
            boolean wasClosed = socket.isClosed();
            socket.close();
            isClosed = true;
            if (!wasClosed && socket.isClosed()) {
                log.info("wrapper " + getHolder().getIdentifier() + " was closed.");
            }
        }
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
        this.isClosed = socket.isClosed();
        log.info("UDP socket was set: " + socket.getInetAddress() + ':' + socket.getPort());
    }
}
