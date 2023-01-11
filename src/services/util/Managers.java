package services.util;

import services.manager.HistoryManager;
import services.manager.InMemoryHistoryManager;
import services.manager.InMemoryTaskManager;
import services.manager.TaskManager;

public final class Managers {

    private Managers() {
    }

    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
