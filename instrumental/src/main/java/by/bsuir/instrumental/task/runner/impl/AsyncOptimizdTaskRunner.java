package by.bsuir.instrumental.task.runner.impl;

import by.bsuir.instrumental.state.application.StateHolder;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.task.runner.TaskRunner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AsyncOptimizdTaskRunner implements TaskRunner {
    private final Task[] tasks;
    private final StateHolder stateHolder;

    @Getter
    @Setter
    private int sleepTime = 50;

    @Override
    public void run() {
        if (tasks.length == 0) {
            stateHolder.setRunning(false);
        }
        while (stateHolder.isRunning()) {

            try {
                for (Task task : tasks) {
                    task.run();
                }
            } catch (RuntimeException e) {
                log.error(e.getMessage());
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }


    @Override
    public void destroy() {
        stateHolder.setRunning(false);
        log.info(this.getClass().getName() + " finished execution.");
    }
}
