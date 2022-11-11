package by.bsuir.instrumental.command.impl;

import by.bsuir.instrumental.command.AbstractCommand;
import by.bsuir.instrumental.input.StructuredCommand;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

public class DirCommand extends AbstractCommand {
    private static final String NO_FILES_FOUND_RESPONCE_MESSAGE = "no files found in directory";

    public DirCommand() {
        super(new String[0], new String[0], new HashMap<>(), new HashMap<>(), "", new Class<?>[0]);
    }

    @Override
    public String execute(StructuredCommand command) {
        String result = "invalid input";
        if (command.getComponents().size() >= 1) {
            String source = command.getComponents().get(0).getValue();
            source = source.substring(1, source.length() - 1);
            String[] fileNames = new File(source).list();
            if (fileNames != null) {
                result = Arrays.stream(fileNames).reduce((s, s1) -> s + ("\n" + s1)).orElse(NO_FILES_FOUND_RESPONCE_MESSAGE);
            } else {
                result = NO_FILES_FOUND_RESPONCE_MESSAGE;
            }
        }
        return result;
    }
}
