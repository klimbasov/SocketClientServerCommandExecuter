package by.bsuir.instrumental.command.impl;

import by.bsuir.instrumental.command.AbstractCommand;
import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.pool.SnapshottingPool;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ShowCommandImpl extends AbstractCommand {
    private static final String HELP_MSG = "use option 'client' to show all clients? connected to the server";
    private static final String DEFAULT_FLAG_NAME = "";
    private static final String[] FLAGS = new String[]{"client", "help"};

    private final Map<String, Supplier<String>> supplierMap = new HashMap<>();

    public ShowCommandImpl(SnapshottingPool wrapperPool) {
        super(FLAGS, new String[]{}, new HashMap<>(), new HashMap<>(), "show", new Class<?>[]{});
        supplierMap.put("help", () -> HELP_MSG);
        supplierMap.put("client", wrapperPool::snapshot);
        supplierMap.put(DEFAULT_FLAG_NAME, () -> HELP_MSG);
    }

    @Override
    public String execute(StructuredCommand command) {
        String retVal = HELP_MSG;
        String supplierAlias = command.getComponents().stream()
                .filter(
                        commandComponent -> commandComponent.getType() == StructuredCommand.CommandComponent.CommandComponentType.OPTION_FULL
                ).findFirst().orElseGet(this::createDefault).getValue();
        Supplier<String> supplier = supplierMap.get(supplierAlias);
        if (supplier != null) {
            retVal = supplier.get();
        }
        return retVal;
    }

    private StructuredCommand.CommandComponent createDefault() {
        StructuredCommand.CommandComponent component = new StructuredCommand.CommandComponent();
        component.setType(StructuredCommand.CommandComponent.CommandComponentType.OPTION_FULL);
        component.setValue(DEFAULT_FLAG_NAME);
        return component;
    }
}
