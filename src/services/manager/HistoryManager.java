package services.manager;

import models.task.Task;

import java.util.HashMap;

public interface HistoryManager {

    void addToHistory(Task task);
    HashMap<Integer, Task> getHistoryMap();
}
