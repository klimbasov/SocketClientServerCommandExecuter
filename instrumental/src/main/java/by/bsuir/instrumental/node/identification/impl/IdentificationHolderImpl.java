package by.bsuir.instrumental.node.identification.impl;

import by.bsuir.instrumental.node.identification.IdentificationHolder;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class IdentificationHolderImpl implements IdentificationHolder {
    private String id;
    private boolean isValid = false;

    public void setId(String id) {
        this.id = id;
        isValid = true;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public boolean isIdentificationValid() {
        return isValid;
    }

    @Override
    public int compareTo(IdentificationHolder o) {
        return id.compareTo(o.getIdentifier());
    }
}
