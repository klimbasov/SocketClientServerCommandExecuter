package by.bsuir.instrumental.state.socket;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ThreadStateHolder {
    private volatile boolean isRunning;
    @Setter
    private String uuid;

    public ThreadStateHolder() {
        this.isRunning = true;
        uuid = "";
    }

    public void stop() {
        isRunning = false;
    }
}
