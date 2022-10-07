package by.bsuir.instrumental.task;

import by.bsuir.instrumental.pool.Pool;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AsyncTaskRunner implements Runnable {
    private final Pool<Task> taskPool;
    @Getter
    @Setter
    private boolean isRunning = true;

    @Override
    public void run() {
        while (isRunning) {
            Optional<Task> optional = taskPool.poll();
            if (optional.isPresent()) {
                Task task = optional.get();
                task.run();
                taskPool.offer(task);  //todo if false returns, some exceptional queue state was happen
            } else {
                isRunning = false;
            }
        }
    }
}
