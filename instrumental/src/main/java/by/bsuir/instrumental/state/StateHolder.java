package by.bsuir.instrumental.state;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StateHolder {
    boolean isRunning = true;
}
