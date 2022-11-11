package by.bsuir.instrumental.command.impl;

import by.bsuir.instrumental.command.AbstractCommand;
import by.bsuir.instrumental.input.StructuredCommand;

import java.util.HashMap;

public class EchoCommand extends AbstractCommand {

    public EchoCommand() {
        super(new String[]{}, new String[]{}, new HashMap<>(), new HashMap<>(), "echo", new Class<?>[]{});
    }

    @Override
    public String execute(StructuredCommand command) {
        StructuredCommand.CommandComponent defaultCommandComponent = new StructuredCommand.CommandComponent()
                .setType(StructuredCommand.CommandComponent.CommandComponentType.ARGUMENT_OR_COMMAND)
                .setValue("");
        return command.getComponents().stream()
                .filter(commandComponent -> commandComponent.getType() == StructuredCommand.CommandComponent.CommandComponentType.ARGUMENT_OR_COMMAND)
                .findFirst().orElse(defaultCommandComponent)
                .getValue();
    }
}
