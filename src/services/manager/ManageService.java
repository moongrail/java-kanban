package services.manager;

import models.task.Epic;
import models.task.SubTask;
import models.task.Task;

import java.util.HashMap;

public interface ManageService {

    HashMap<Integer, Task> getAllTaskList();

    boolean removeAllTasks();

    Task getTaskById(Integer id);

    Task addTask(Task task);

    Task addTask(Epic task);

    Task addSubTask(Integer idEpic, SubTask subTask);

    Task updateTask(Task task);

    Epic updateTask(Epic task);

    SubTask updateTask(SubTask task);

    boolean removeTaskById(Integer id);

}
