package by.bsuir.instrumental.task.runner.impl;

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
    @Getter
    @Setter
    private boolean isRunning = true;

    @Getter
    @Setter
    private int sleepTime = 50;

    @Override
    public void run() {
        if(tasks.length == 0){
            isRunning = false;
        }
        while (isRunning) {
            for (Task task: tasks){
                task.run();
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }


    @Override
    public void destroy(){
        this.isRunning = false;
    }
}
