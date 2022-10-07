package by.bsuir.instrumental.command.impl;

import by.bsuir.instrumental.command.AbstractCommand;
import by.bsuir.instrumental.input.StructuredCommand;

import java.time.LocalDateTime;
import java.util.Map;

public class TimeCommand extends AbstractCommand {
    public TimeCommand(String[] flags, String[] shortenFlags, Map<String, Class<?>> options, Map<String, Class<?>> shortenOptions, String name) {
        super(flags, shortenFlags, options, shortenOptions, name);
    }

    @Override
    public String execute(StructuredCommand command) {
        return LocalDateTime.now().toString();
    }
}
