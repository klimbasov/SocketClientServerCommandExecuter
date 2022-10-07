package by.bsuir.instrumental.command.factory;

import by.bsuir.instrumental.input.StructuredCommand;

public interface CommandFactory {
    String execute(StructuredCommand structuredCommand);
}
