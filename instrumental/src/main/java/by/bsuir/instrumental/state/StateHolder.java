package by.bsuir.instrumental.state;

import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.node.AbstractNodeIOWrapper;
import by.bsuir.instrumental.slftp.SlftpController;
import by.bsuir.instrumental.task.runner.TaskRunner;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StateHolder {
    boolean isRunning = true;
}
