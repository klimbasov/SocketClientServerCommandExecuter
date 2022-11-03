package by.bsuir.instrumental.task.runner.impl;

import by.bsuir.instrumental.pool.QueuePool;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.task.runner.TaskRunner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncTaskRunner implements TaskRunner {
    private final QueuePool<Task> taskQueuePool;
    @Getter
    @Setter
    private boolean isRunning = true;

    @Getter
    @Setter
    private int sleepTime = 50;

    @Override
    public void run() {
        while (isRunning) {
            Optional<Task> optional = taskQueuePool.poll();
            if (optional.isPresent()) {
                Task task = optional.get();
                try {
                    task.run();
                }catch (RuntimeException e){
                    log.error(e.getMessage());
                }
                taskQueuePool.offer(task);  //todo if false returns, some exceptional queue state was happen
            } else {
                isRunning = false;
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
        this.isRunning = false;
    }
}
