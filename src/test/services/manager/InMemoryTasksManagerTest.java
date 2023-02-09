package test.services.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import services.manager.InMemoryTaskManager;
import services.manager.TaskManager;

@DisplayName("InMemoryTaskManager default test.")
public class InMemoryTasksManagerTest<T extends TaskManager> extends TaskManagerTest<T> {

    @BeforeEach
    public void BeforeEach(){
        taskManager = (T) new InMemoryTaskManager();
    }

}
