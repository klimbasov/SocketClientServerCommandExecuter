package by.bsuir.server.command.impl;

import by.bsuir.server.command.AbstractCommand;
import by.bsuir.server.proccessor.input.StructedCommand;

import java.time.LocalDateTime;
import java.util.Map;

public class TimeCommandImpl extends AbstractCommand {
    protected TimeCommandImpl(String[] flags, String[] shortenFlags, Map<String, Class<?>> options, Map<String, Class<?>> shortenOptions, String name) {
        super(flags, shortenFlags, options, shortenOptions, name);
    }

    @Override
    public String execute(StructedCommand command) {
        return LocalDateTime.now().toString();
    }
}
