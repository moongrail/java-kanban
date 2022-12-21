package services.manager;

import models.task.Task;

import java.util.HashMap;

public class HistoryManagerServiceImpl implements HistoryManager {
    private int historyId = 1;

    private final HashMap<Integer, Task> historyReadStatistic = new HashMap<>();

    public void addToHistory(Task task) {
        if (historyId > 10) {
            historyReadStatistic.remove(historyId - historyReadStatistic.size());
        }
        historyReadStatistic.put(historyId, task);
        historyId++;
    }

    public HashMap<Integer, Task> getHistoryMap() {
        return historyReadStatistic;
    }
}
