package by.bsuir.instrumental.command.impl;

import by.bsuir.instrumental.command.AbstractCommand;
import by.bsuir.instrumental.service.MetaDataRecord;
import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.slftp.SlftpController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

@Component
public class CopyFileCommand extends AbstractCommand {
    @Value("${command.copy.metadataFileLocation:'./'}")
    private String METADATA_FILE_LOCATION;

    private static final String COMMAND_NAME = "copy";
    private static final Map<String, Class<?>> potions = Map.ofEntries(
            entry("source", String.class)
    );
    private static final Map<String, Class<?>> SHORTEN_OPTIONS = new HashMap<>();
    private static final Class<?>[] ARGUMENTS = new Class[]{String.class, String.class};

    private final SlftpController controller;


    public CopyFileCommand(SlftpController controller) {
        super(new String[0], new String[0], potions, SHORTEN_OPTIONS, COMMAND_NAME, ARGUMENTS);
        this.controller = controller;
    }

    @Override
    public String execute(StructuredCommand command) {
        String result = "transferring initiated";
        if(command.getComponents().size()>=2){
            String source = command.getComponents().get(0).getValue();
            source = source.substring(1, source.length()-1);
            String host = command.getComponents().get(1).getValue();
            host = host.substring(1, host.length()-1);
            Path path = Path.of(source);
            if(Files.exists(path)){
                result = "file " + source + " exists, starting transferring";
                controller.initCommunicationWithFileName(source, host);
            }

        }
        return result;

    }

    void createMetadataRecord(){

    }
}
