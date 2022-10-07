package by.bsuir.instrumental.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

@Component      //todo think of static util class usage
public class PlainCommandProcessor {
    public static final String COMMAND_DELIMITER = "\\\\";
    public static final Pattern COMMAND_PATTERN = Pattern.compile("^\\w+");

    private static void structCommandSignature(String trimmedAtomicCommand, StructuredCommand structuredCommand) {
        Matcher commandComponentsMatcher = StructuredCommand.CommandComponent.CommandComponentType.getPattern().matcher(trimmedAtomicCommand);
        int groupCount = commandComponentsMatcher.groupCount();
        while (commandComponentsMatcher.find()) {
            for (int groupCounter = 0; groupCounter < groupCount; groupCounter++) {
                String foundString = commandComponentsMatcher.group(groupCounter);
                if (nonNull(foundString)) {
                    StructuredCommand.CommandComponent component = new StructuredCommand.CommandComponent()
                            .setType(StructuredCommand.CommandComponent.CommandComponentType.getTypeByPatternGroup(groupCounter).orElseThrow())
                            .setValue(foundString);
                    structuredCommand.getComponents().add(component);
                    break;
                }
            }
        }
    }

    private static void setCommandName(String atomicCommands, StructuredCommand structuredCommand) {
        Matcher commandNameMatcher = COMMAND_PATTERN.matcher(atomicCommands);
        if (!commandNameMatcher.find()) {
            throw new RuntimeException();
        }
        String commandName = commandNameMatcher.group();
        structuredCommand.setCommand(commandName);
    }

    public List<StructuredCommand> process(@NotNull @NotEmpty String input) {
        List<StructuredCommand> structuredCommandList = new LinkedList<>();
        String[] atomicCommandsArray = input.split(COMMAND_DELIMITER);
        for (String atomicCommands : atomicCommandsArray) {
            String trimmedAtomicCommand = atomicCommands.trim();
            StructuredCommand structuredCommand = new StructuredCommand();
            setCommandName(atomicCommands, structuredCommand);
            structCommandSignature(trimmedAtomicCommand, structuredCommand);
            structuredCommandList.add(structuredCommand);
        }
        return structuredCommandList;
    }
}
