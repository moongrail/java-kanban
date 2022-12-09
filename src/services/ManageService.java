package services;

import models.task.Task;

import java.util.HashMap;

public interface ManageService {

    HashMap<Integer, Task> getAllTaskList();
    boolean removeAllTasks();
    Task getTaskById(Integer id);
    Task createTask(Task task);
    Task updateTask(Integer id);
    boolean removeTaskById(Integer id);

}
