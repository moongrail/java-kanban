package services.util;

import services.manager.*;

import java.io.File;

public final class Managers {

    private Managers() {
    }

    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getDefaultFileBackedTasksManager(File file) {
        return new FileBackedTasksManager(file);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
