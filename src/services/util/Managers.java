package services.util;

import services.manager.HistoryManager;
import services.manager.HistoryManagerServiceImpl;
import services.manager.InMemoryTaskManager;
import services.manager.TaskManager;

public class Managers {
    public static TaskManager getDefaultTaskManager(){
        return new InMemoryTaskManager();
    }
    public static HistoryManager getDefaultHistory(){
        return new HistoryManagerServiceImpl();
    }
}
