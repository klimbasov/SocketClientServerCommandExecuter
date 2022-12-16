package by.bsuir.instrumental.task.runner.impl;

import by.bsuir.instrumental.state.application.StateHolder;
import by.bsuir.instrumental.task.InfiniteTask;
import by.bsuir.instrumental.task.Task;
import by.bsuir.instrumental.task.runner.TaskRunner;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class MultithreadingTaskRunner implements TaskRunner {
    private final InfiniteTask[] threadTasks;
    private final Task[] tasks;
    private final List<Thread> threads;
    private final StateHolder stateHolder;

    @Getter
    @Setter
    private int sleepTime = 50;

    public MultithreadingTaskRunner(InfiniteTask[] threadTasks, Task[] tasks, StateHolder stateHolder) {
        this.tasks = tasks;
        this.threadTasks = threadTasks;
        this.stateHolder = stateHolder;
        threads = new LinkedList<>();
    }

    @Override
    public void destroy() {
        stateHolder.setRunning(false);
        Arrays.stream(threadTasks).forEach(InfiniteTask::stop);
        threads.forEach(thread -> {
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                log.error("Thread " + thread.getName() + " cannot be joined");
            }
        });
    }

    @Override
    public void run() {
        Arrays.stream(threadTasks).forEach(infiniteTask -> {
            Thread thread = new Thread(infiniteTask);
            thread.start();
            threads.add(thread);
        });

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
}
