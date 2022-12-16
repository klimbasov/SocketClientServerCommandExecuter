package by.bsuir.instrumental.command.ui;

import by.bsuir.instrumental.state.application.StateHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.util.Scanner;

@RequiredArgsConstructor
@Slf4j
public class SystemInputProducer implements Runnable, DisposableBean {
    private static final String STOP_WORD = "quit";
    private final StateHolder stateHolder;
    private final InputQueuePool inputPool;
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        while (stateHolder.isRunning()) {
            String input = scanner.nextLine();
            if (input.contains(STOP_WORD)) {
                stateHolder.setRunning(false);
            } else {
                inputPool.offer(input);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        log.info(this.getClass().getName() + " finished execution.");
        stateHolder.setRunning(false);
    }
}
