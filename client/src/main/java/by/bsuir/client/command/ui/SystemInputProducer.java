package by.bsuir.client.command.ui;

import by.bsuir.instrumental.state.StateHolder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemInputProducer implements Runnable, DisposableBean {
    private final StateHolder stateHolder;
    private final InputQueuePool inputPool;
    private final Scanner scanner = new Scanner(System.in);

    private static final String STOP_WORD = "quit";

    @Override
    public void run() {
        while (stateHolder.isRunning()) {
            String input = scanner.nextLine();
            if(input.contains(STOP_WORD)){
                stateHolder.setRunning(false);
            }else {
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
