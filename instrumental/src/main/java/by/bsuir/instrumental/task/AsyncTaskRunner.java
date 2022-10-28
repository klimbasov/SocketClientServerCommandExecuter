package by.bsuir.instrumental.task;

import by.bsuir.instrumental.pool.Pool;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncTaskRunner implements Runnable, DisposableBean {
    private final Pool<Task> taskPool;
    @Getter
    @Setter
    private boolean isRunning = true;

    @Getter
    @Setter
    private int sleepTime = 50;

    @Override
    public void run() {
        while (isRunning) {
            Optional<Task> optional = taskPool.poll();
            if (optional.isPresent()) {
                Task task = optional.get();
                try {
                    task.run();
                }catch (RuntimeException e){
                    log.error(e.getMessage());
                }
                taskPool.offer(task);  //todo if false returns, some exceptional queue state was happen
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
    public void destroy() throws Exception {
        this.isRunning = false;
    }
}
