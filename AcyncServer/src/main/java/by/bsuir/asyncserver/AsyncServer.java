package by.bsuir.asyncserver;

import by.bsuir.instrumental.task.runner.TaskRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@RequiredArgsConstructor
public class AsyncServer implements CommandLineRunner {
    private final TaskRunner runner;
    public static void main(String[] args) {
        new SpringApplicationBuilder(AsyncServer.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) {
        runner.run();
    }
}
