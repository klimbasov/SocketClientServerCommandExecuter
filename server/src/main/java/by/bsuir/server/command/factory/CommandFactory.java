package by.bsuir.server.command.factory;

import by.bsuir.server.proccessor.input.StructedCommand;

public interface CommandFactory {
    void execute(StructedCommand structedCommand);
}
