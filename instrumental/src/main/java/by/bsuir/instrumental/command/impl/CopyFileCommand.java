package by.bsuir.instrumental.command.impl;

import by.bsuir.instrumental.command.AbstractCommand;
import by.bsuir.instrumental.ftp.FtpController;
import by.bsuir.instrumental.input.StructuredCommand;
import by.bsuir.instrumental.node.identification.IdentificationHolder;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

@Component
public class CopyFileCommand extends AbstractCommand {
    private static final String COMMAND_NAME = "copy";
    private static final Map<String, Class<?>> potions = Map.ofEntries(
            entry("source", String.class)
    );
    private static final Map<String, Class<?>> SHORTEN_OPTIONS = new HashMap<>();
    private static final Class<?>[] ARGUMENTS = new Class[]{String.class, String.class};
    private static final String CAN_NOT_PROCESS_MSG = "request can not be processed. Invalid addresses were set";
    private final FtpController controller;
    private final IdentificationHolder holder;


    public CopyFileCommand(FtpController controller, IdentificationHolder holder) {
        super(new String[0], new String[0], potions, SHORTEN_OPTIONS, COMMAND_NAME, ARGUMENTS);
        this.controller = controller;
        this.holder = holder;
    }

    @Override
    public String execute(StructuredCommand command) {
        String result = "transferring initiated";
        if (command.getComponents().size() >= 3) {
            String url = parseStringArg(command, 0);
            String source = parseStringArg(command, 1);
            String destination = parseStringArg(command, 2);
            Path path = Path.of(url);
            if (source.equals(holder.getIdentifier())) {
                if (Files.exists(path)) {
                    result = "file " + url + " exists, starting transferring";
                    controller.upload(url, destination);
                } else {
                    result = "there is no such file";
                }
            } else if (destination.equals(holder.getIdentifier())) {
                controller.download(url, source);
            } else {
                result = CAN_NOT_PROCESS_MSG;
            }

        }
        return result;
    }

    private String parseStringArg(StructuredCommand command, int argNum) {
        String arg = command.getComponents().get(argNum).getValue();
        return arg.substring(1, arg.length() - 1);
    }
}
