package by.bsuir.instrumental.node.identification.token.impl;

import by.bsuir.instrumental.node.identification.token.AuthenticationToken;
import lombok.Value;

@Value
public class UsernameAuthenticationToken implements AuthenticationToken {
    String username;

    @Override
    public String getPublicName() {
        return username;
    }
}
