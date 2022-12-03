package by.bsuir.instrumental.node;

import by.bsuir.instrumental.packet.Packet;
import by.bsuir.instrumental.packet.PacketFlags;
import by.bsuir.instrumental.packet.type.PacketType;
import by.bsuir.instrumental.pool.UuidAddressTable;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.util.Objects.nonNull;

@Slf4j
public class UdpSocketIOWrapper extends AbstractNodeIOWrapper{
    private static final byte[] OBJECT_STREAM_HEADER = new byte[]{-84, -19, 0, 5};
    private static final int OBJECT_STREAM_HEADER_LENGTH = 4;
    private static final int DEFAULT_SO_TIMEOUT = 50;
    private static final Packet FAULT_ADDRESS_NOT_FOUND_PACKET = new Packet("no root found for request".getBytes(), "".getBytes(),
            "".getBytes(), PacketType.INFORM_PACKAGE.typeId, PacketFlags.ACK.flagValue);
    private static final int DATAGRAM_PACKET_SIZE = 1024 << 2;
    private DatagramSocket socket;
    private final Queue<Packet> callbackQueue;
    private final UuidAddressTable addressTable;

    private boolean isClosed;

    public UdpSocketIOWrapper(UuidAddressTable addressTable) {
        super(null);
        this.callbackQueue = new LinkedList<>();
        this.addressTable = addressTable;
        this.isClosed = true;
    }

    @Override
    public Optional<Packet> receive() {
        byte[] data = new byte[DATAGRAM_PACKET_SIZE];
        DatagramPacket datagramPacket = new DatagramPacket(data, DATAGRAM_PACKET_SIZE);
        Optional<Packet> optional = Optional.empty();
        if(callbackQueue.isEmpty()){
            optional = receiveFromSocket(datagramPacket, optional);
        }else {
            optional = Optional.of(callbackQueue.poll());
        }


        return optional;
    }

    @Override
    public void send(Packet response) {
        String uuid = new String(response.getTargetId());
        SocketAddress address = addressTable.get(uuid);
        if(nonNull(address)){
            sendToSocket(response, address);
        }else {
            callbackQueue.add(FAULT_ADDRESS_NOT_FOUND_PACKET);
        }
    }

    private void sendToSocket(Packet response, SocketAddress address) {
        DatagramPacket packet;
        byte[] data;
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)){
            objectOutputStream.writeObject(response);
            data = Arrays.copyOf(byteArrayOutputStream.toByteArray(), DATAGRAM_PACKET_SIZE);
            packet = new DatagramPacket(data, DATAGRAM_PACKET_SIZE);
            packet.setSocketAddress(address);
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Packet> receiveFromSocket(DatagramPacket datagramPacket, Optional<Packet> optional) {
        try {
            socket.receive(datagramPacket);
            try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(datagramPacket.getData());
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)){
                Packet packet = (Packet) objectInputStream.readObject();
                optional = Optional.of(packet);
                String source = new String(packet.getSourceId());
                SocketAddress address = datagramPacket.getSocketAddress();
                if(!address.equals(addressTable.get(source))){
                    addressTable.put(source, address);
                }
            } catch (ClassNotFoundException e) {
                log.warn("received packet was corrupted");
            }

        }catch (SocketTimeoutException ignored){}
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return optional;
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
    }
}
