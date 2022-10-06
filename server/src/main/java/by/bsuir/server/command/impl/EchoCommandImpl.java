package by.bsuir.server.command.impl;

import by.bsuir.server.command.AbstractCommand;
import by.bsuir.server.proccessor.input.StructedCommand;

import java.util.HashMap;
import java.util.Map;

public class EchoCommandImpl extends AbstractCommand {

    protected EchoCommandImpl(Map<String, Class<?>> options, Map<String, Class<?>> shortenOptions, String name) {
        super(new String[]{}, new String[]{}, new HashMap<>(), new HashMap<>(), "");
    }

    @Override
    public String execute(StructedCommand command) {
        StructedCommand.CommandComponent defaultCommandComponent = new StructedCommand.CommandComponent()
                .setType(StructedCommand.CommandComponent.CommandComponentType.ARGUMENT)
                .setValue("");
        return command.getComponents().stream()
                .filter(commandComponent -> commandComponent.getType() == StructedCommand.CommandComponent.CommandComponentType.ARGUMENT)
                .findFirst().orElse(defaultCommandComponent)
                .getValue();
    }
}
