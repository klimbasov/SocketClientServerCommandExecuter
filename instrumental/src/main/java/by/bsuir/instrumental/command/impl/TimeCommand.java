package by.bsuir.instrumental.command.impl;

import by.bsuir.instrumental.command.AbstractCommand;
import by.bsuir.instrumental.input.StructuredCommand;

import java.time.LocalDateTime;
import java.util.HashMap;

public class TimeCommand extends AbstractCommand {
    public TimeCommand() {
        super(new String[]{}, new String[]{}, new HashMap<>(), new HashMap<>(), "time", new Class<?>[]{});
    }

    @Override
    public String execute(StructuredCommand command) {
        return LocalDateTime.now().toString();
    }
}
