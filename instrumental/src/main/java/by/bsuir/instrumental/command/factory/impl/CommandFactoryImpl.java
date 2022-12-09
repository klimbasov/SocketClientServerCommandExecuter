package by.bsuir.instrumental.command.factory.impl;

import by.bsuir.instrumental.command.Command;
import by.bsuir.instrumental.command.factory.CommandFactory;
import by.bsuir.instrumental.command.impl.DirCommand;
import by.bsuir.instrumental.command.impl.EchoCommand;
import by.bsuir.instrumental.command.impl.ShowCommandImpl;
import by.bsuir.instrumental.command.impl.TimeCommand;
import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.pool.Snapshot;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

public class CommandFactoryImpl implements CommandFactory {
    private static final String COMMAND_NOT_FOUND_RESPONSE = "command not found";
    private final Map<String, Command> commandMap = new HashMap<>();

    public CommandFactoryImpl() {
        initCommandMap();
    }

    @Override
    public String execute(StructuredCommand structuredCommand) {
        String response = COMMAND_NOT_FOUND_RESPONSE;
        String commandName = structuredCommand.getCommand();
        Command command = commandMap.get(commandName);
        if (nonNull(command)) {
            response = command.execute(structuredCommand);
        }
        return response;
    }

    public void addCommand(String alias, Command command) {
        commandMap.put(alias, command);
    }

    private void initCommandMap() {
        commandMap.put("echo", new EchoCommand());
        commandMap.put("time", new TimeCommand());
        commandMap.put("dir", new DirCommand());
    }

    public void setWrapperPool(Snapshot wrapperPool) {
        ShowCommandImpl showCommand = new ShowCommandImpl(wrapperPool);
        commandMap.put("show", showCommand);
    }
}
