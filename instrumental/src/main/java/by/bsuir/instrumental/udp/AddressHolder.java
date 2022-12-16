package by.bsuir.instrumental.udp;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AddressHolder {
    String address;
    String port;
}
