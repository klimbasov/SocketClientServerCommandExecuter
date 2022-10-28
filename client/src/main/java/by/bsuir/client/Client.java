package by.bsuir.client;

import by.bsuir.client.command.ui.SystemInputProducer;
import by.bsuir.instrumental.task.runner.TaskRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@RequiredArgsConstructor
public class Client implements CommandLineRunner {

    private final TaskRunner runner;
    private final SystemInputProducer systemInputProducer;

    public static void main(String[] args) {
        new SpringApplicationBuilder(Client.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        Thread inputThread = new Thread(systemInputProducer);
        inputThread.start();
        runner.run();
        inputThread.join();
    }
}
