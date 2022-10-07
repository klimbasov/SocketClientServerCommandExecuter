package by.bsuir.instrumental.input;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Getter
@Setter
@Accessors(chain = true)
public class StructuredCommand {
    private String command;
    private List<CommandComponent> components;

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CommandComponent {
        private String value;
        private CommandComponentType type;

        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public enum CommandComponentType {
            OPTION_SHORTEN(new int[]{2}),
            OPTION_FULL(new int[]{1}),
            ARGUMENT(new int[]{3, 4});


            private static final Pattern pattern = Pattern.compile("((?<= -)[A-Za-z]+(?=\\s|$))|((?<= --)[A-Za-z]+(?=[ +|$]))|((?:\"[^\"]*\")*\"[\\sA-Za-z0-9\\/.][\\sA-Za-z0-9-_\\/]*\")|([A-Za-z0-9\\/.][A-Za-z0-9-_\\/]*)");
            private final int[] groupsMatched;

            public static Pattern getPattern() {
                return pattern;
            }

            public static Optional<CommandComponentType> getTypeByPatternGroup(int group) {
                Optional<CommandComponentType> optionalCommandComponentType = Optional.empty();
                for (CommandComponentType componentType : CommandComponentType.values()) {
                    for (int typeGroup : componentType.groupsMatched) {
                        if (typeGroup == group) {
                            optionalCommandComponentType = Optional.of(componentType);
                            break;
                        }
                    }
                }
                return optionalCommandComponentType;
            }
        }

    }
}
