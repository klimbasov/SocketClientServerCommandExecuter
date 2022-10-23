package by.bsuir.instrumental.node.token.impl;

import by.bsuir.instrumental.node.token.IdentificationHolder;
import org.springframework.stereotype.Component;

@Component
public class IdentificationHolderImpl implements IdentificationHolder {
    private byte[] id = new byte[0];
    private boolean isValid = false;

    public void setId(byte[] id) {
        this.id = id;
        isValid = true;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public byte[] getIdentifier() {
        return new byte[0];
    }

    @Override
    public boolean isIdentificationValid() {
        return isValid;
    }
}
