package by.bsuir.client.command.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class SystemInputProducer implements Runnable, DisposableBean {
    private final InputQueuePool inputPool;
    private final Scanner scanner = new Scanner(System.in);
    private boolean isRunning = true;

    @Override
    public void run() {
        while (isRunning) {
            String input = scanner.nextLine();
            inputPool.offer(input);
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    @Override
    public void destroy() throws Exception {
        this.isRunning = false;
    }
}
