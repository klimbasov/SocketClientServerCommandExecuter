package by.bsuir.instrumental.state.application;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StateHolder {
    volatile boolean isRunning = true;
}
