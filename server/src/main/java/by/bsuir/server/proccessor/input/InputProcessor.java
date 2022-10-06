package by.bsuir.server.proccessor.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

public class InputProcessor {
    public static final String COMMAND_DELIMITER = "\\\\";
    public static final Pattern COMMAND_PATTERN = Pattern.compile("^\\w+");

    private static void structCommandSignature(String trimmedAtomicCommand, StructedCommand structedCommand) {
        Matcher commandComponentsMatcher = StructedCommand.CommandComponent.CommandComponentType.getPattern().matcher(trimmedAtomicCommand);
        int groupCount = commandComponentsMatcher.groupCount();
        while (commandComponentsMatcher.find()) {
            for (int groupCounter = 0; groupCounter < groupCount; groupCounter++) {
                String foundString = commandComponentsMatcher.group(groupCounter);
                if (nonNull(foundString)) {
                    StructedCommand.CommandComponent component = new StructedCommand.CommandComponent()
                            .setType(StructedCommand.CommandComponent.CommandComponentType.getTypeByPatternGroup(groupCounter).orElseThrow())
                            .setValue(foundString);
                    structedCommand.getComponents().add(component);
                    break;
                }
            }
        }
    }

    private static void setCommandName(String atomicCommands, StructedCommand structedCommand) {
        Matcher commandNameMatcher = COMMAND_PATTERN.matcher(atomicCommands);
        if (!commandNameMatcher.find()) {
            throw new RuntimeException();
        }
        String commandName = commandNameMatcher.group();
        structedCommand.setCommand(commandName);
    }

    public List<StructedCommand> process(@NotNull @NotEmpty String input) {
        List<StructedCommand> structuredCommandList = new LinkedList<>();
        String[] atomicCommandsArray = input.split(COMMAND_DELIMITER);
        for (String atomicCommands : atomicCommandsArray) {
            String trimmedAtomicCommand = atomicCommands.trim();
            StructedCommand structedCommand = new StructedCommand();
            setCommandName(atomicCommands, structedCommand);
            structCommandSignature(trimmedAtomicCommand, structedCommand);
            structuredCommandList.add(structedCommand);
        }
        return structuredCommandList;
    }
}
