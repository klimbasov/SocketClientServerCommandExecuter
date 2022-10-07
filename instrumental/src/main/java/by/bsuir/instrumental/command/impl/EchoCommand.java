package by.bsuir.instrumental.command.impl;

import by.bsuir.instrumental.command.AbstractCommand;
import by.bsuir.instrumental.input.StructuredCommand;

import java.util.HashMap;
import java.util.Map;

public class EchoCommand extends AbstractCommand {

    public EchoCommand(Map<String, Class<?>> options, Map<String, Class<?>> shortenOptions, String name) {
        super(new String[]{}, new String[]{}, new HashMap<>(), new HashMap<>(), "");
    }

    @Override
    public String execute(StructuredCommand command) {
        StructuredCommand.CommandComponent defaultCommandComponent = new StructuredCommand.CommandComponent()
                .setType(StructuredCommand.CommandComponent.CommandComponentType.ARGUMENT)
                .setValue("");
        return command.getComponents().stream()
                .filter(commandComponent -> commandComponent.getType() == StructuredCommand.CommandComponent.CommandComponentType.ARGUMENT)
                .findFirst().orElse(defaultCommandComponent)
                .getValue();
    }
}
