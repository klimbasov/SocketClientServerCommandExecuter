package by.bsuir.server.command;

import by.bsuir.server.proccessor.input.StructedCommand;

public interface Command {
    String execute(StructedCommand command);
}
