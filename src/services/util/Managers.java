package services.util;

import services.manager.InMemoryTaskManager;
import services.manager.TaskManager;

public class Managers<T extends TaskManager> {
    public static TaskManager getDefault(){
        return new InMemoryTaskManager();
    }
}
