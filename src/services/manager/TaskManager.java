package services.manager;

import models.task.Epic;
import models.task.SubTask;
import models.task.Task;

import java.util.HashMap;
import java.util.List;

public interface TaskManager {

    HashMap<Integer, Task> getAllMap();
    HashMap<Integer, Epic> getAllEpicMap();
    HashMap<Integer, SubTask> getAllSubTaskMap();
    HashMap<Integer, Task> getAllTaskMap();
    Task getTaskById(Integer id);
    Epic getEpicById(Integer id);
    SubTask getSubTaskById(Integer id);
    void addTask(Task task);
    void addEpic(Epic task);
    void addSubTask(Integer idEpic, SubTask subTask);
    void updateTask(Task task);
    void updateEpic(Epic task);
    void updateSubTask(SubTask task);
    void removeTaskById(Integer id);
    void removeEpicById(Integer id);
    void removeSubTask(Integer id);
    void removeAll();
    void removeTaskMap();
    void removeEpicMap();
    void removeSubTaskMap();
    List<SubTask> getSubTasksByEpic(Epic epic);
    List<Task> getHistory();

}
