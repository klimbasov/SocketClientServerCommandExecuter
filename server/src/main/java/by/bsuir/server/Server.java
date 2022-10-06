package by.bsuir.server;

import by.bsuir.server.task.AsyncTaskRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@RequiredArgsConstructor
public class Server implements CommandLineRunner {
    private final AsyncTaskRunner runner;

    public static void main(String[] args) {
        new SpringApplicationBuilder().web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) {
        runner.run();
    }
}
