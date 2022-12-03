package by.bsuir.instrumental.udp;

import lombok.Builder;
import lombok.Value;

import java.net.InetAddress;

@Value
@Builder
public class AddressHolder {
    String address;
    String port;
}
