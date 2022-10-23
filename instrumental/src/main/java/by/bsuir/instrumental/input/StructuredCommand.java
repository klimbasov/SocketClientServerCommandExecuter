package by.bsuir.instrumental.input;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class StructuredCommand implements Serializable {
    private String command;
    private String targetIdentifier;
    private List<CommandComponent> components;

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CommandComponent {
        private String value;
        private CommandComponentType type;

        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public enum CommandComponentType {
            ADDRESS,
            OPTION_SHORTEN,
            OPTION_FULL,
            ARGUMENT_OR_COMMAND,
            UNKNOWN;

            public static CommandComponentType getTypeByPatternGroup(int group) {
                return switch (group) {
                    case 1 -> ADDRESS;
                    case 2 -> OPTION_SHORTEN;
                    case 3 -> OPTION_FULL;
                    case 4,5 -> ARGUMENT_OR_COMMAND;
                    default -> UNKNOWN;
                };
            }
        }

    }
}
