package by.bsuir.instrumental.command.impl;

import by.bsuir.instrumental.command.AbstractCommand;
import by.bsuir.instrumental.service.MetaDataRecord;
import by.bsuir.instrumental.input.StructuredCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

@Component
public class CopyFileCommand extends AbstractCommand {
    @Value("${command.copy.metadataFileLocation:'./'}")
    private String METADATA_FILE_LOCATION;

    private static final String COMMAND_NAME = "copy";
    private static final Map<String, Class<?>> potions = Map.ofEntries(
            entry("particleSize", Integer.class)
    );
    private static final Map<String, Class<?>> SHORTEN_OPTIONS = new HashMap<>();
    private static final Class<?>[] ARGUMENTS = new Class[]{String.class, String.class};
    private static final String[] SHORTEN_FLAGS = new String[0];
    private static final String[] flags = new String[]{"silence"};

    private static final Map<String, MetaDataRecord> metaFileMap = new HashMap<>();

    public CopyFileCommand() {
        super(flags, SHORTEN_FLAGS, potions, SHORTEN_OPTIONS, COMMAND_NAME, ARGUMENTS);
    }

    @Override
    public String execute(StructuredCommand command) {

        return null;
    }

    void createMetadataRecord(){

    }
}
