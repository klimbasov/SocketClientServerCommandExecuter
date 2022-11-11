package by.bsuir.client.command.ui;

import by.bsuir.instrumental.input.StructuredCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

@Component
public class RawInputStructuredCommandAdapter {
    public static final String COMMAND_DELIMITER = "\\\\";
    private static final Pattern COMPONENTS_PATTERN = Pattern.compile("([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+(?=\\s|$))|((?<= -)[A-Za-z]+(?=\\s|$))|((?<= --)[A-Za-z]+(?=[ +]|$))|((?:\"[^\"]*\")*\"[\\sA-Za-z0-9\\/.]*[\\sA-Za-z0-9-_\\/]*\")|([A-Za-z0-9\\/.][A-Za-z0-9-_\\/]*)");

    public List<StructuredCommand> toStructuredCommand(String input) {
        List<StructuredCommand> structuredCommandList = new LinkedList<>();
        String[] atomicCommandsArray = input.split(COMMAND_DELIMITER);
        for (String atomicCommand : atomicCommandsArray) {
            StructuredCommand structuredCommand = new StructuredCommand();

            List<StructuredCommand.CommandComponent> components = structCommandSignature(atomicCommand);
            structuredCommand
                    .setTargetIdentifier(components.get(0).getValue())
                    .setCommand(components.get(1).getValue())
                    .setComponents(getComponents(components));
            structuredCommandList.add(structuredCommand);
        }
        return structuredCommandList;
    }

    private List<StructuredCommand.CommandComponent> structCommandSignature(String trimmedAtomicCommand) {
        List<StructuredCommand.CommandComponent> components = new LinkedList<>();
        Matcher commandComponentsMatcher = COMPONENTS_PATTERN.matcher(trimmedAtomicCommand);
        int groupCount = commandComponentsMatcher.groupCount();
        while (commandComponentsMatcher.find()) {
            for (int groupCounter = 1; groupCounter <= groupCount; groupCounter++) {
                String foundString = commandComponentsMatcher.group(groupCounter);
                if (nonNull(foundString)) {
                    StructuredCommand.CommandComponent component = new StructuredCommand.CommandComponent()
                            .setType(StructuredCommand.CommandComponent.CommandComponentType.getTypeByPatternGroup(groupCounter))
                            .setValue(foundString);
                    components.add(component);
                    break;
                }
            }
        }
        validate(components);
        return components;
    }

    private List<StructuredCommand.CommandComponent> getComponents(List<StructuredCommand.CommandComponent> command) {
        List<StructuredCommand.CommandComponent> components = new ArrayList<>();
        if (command.size() >= 3) {
            components = new ArrayList<>(command.subList(2, command.size()));
        }
        return components;
    }

    private void validate(List<StructuredCommand.CommandComponent> components) {
        if (components.isEmpty() || components.size() < 2) {
            throw new RuntimeException("command dose not correct. At least ip and command identifier must be specified");
        }
        StructuredCommand.CommandComponent addressComponent = components.get(0);
        StructuredCommand.CommandComponent commandNameComponent = components.get(1);
        if (addressComponent.getType() != StructuredCommand.CommandComponent.CommandComponentType.ADDRESS ||
                commandNameComponent.getType() != StructuredCommand.CommandComponent.CommandComponentType.ARGUMENT_OR_COMMAND) {
            throw new RuntimeException("command is not correct. Ip or command identifier are invalid");
        }
    }
}
