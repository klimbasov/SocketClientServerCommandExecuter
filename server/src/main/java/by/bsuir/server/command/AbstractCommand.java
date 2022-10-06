package by.bsuir.server.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractCommand implements Command {
    protected final String[] flags;
    protected final String[] shortenFlags;
    protected final Map<String, Class<?>> options;
    protected final Map<String, Class<?>> shortenOptions;
    protected final String name;
}
