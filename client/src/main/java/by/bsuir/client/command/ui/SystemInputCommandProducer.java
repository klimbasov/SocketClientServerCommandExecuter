package by.bsuir.client.command.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class SystemInputCommandProducer implements Runnable{
    private boolean isRunning = true;
    private final InputPool inputPool;
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        while (isRunning){
            String input = scanner.nextLine();
            inputPool.offer(input);
        }
    }

    public void stop(){
        this.isRunning = false;
    }
}
