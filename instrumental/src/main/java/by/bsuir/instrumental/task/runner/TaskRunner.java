package by.bsuir.instrumental.task.runner;

import org.springframework.beans.factory.DisposableBean;

public interface TaskRunner extends Runnable, DisposableBean {
    void destroy();
}
