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
    Task addTask(Task task);
    Epic addEpic(Epic task);
    SubTask addSubTask(Integer idEpic, SubTask subTask);
    Task updateTask(Task task);
    Epic updateEpic(Epic task);
    SubTask updateSubTask(SubTask task);
    boolean removeTaskById(Integer id);
    boolean removeEpicById(Integer id);
    boolean removeSubTask(Integer id);
    boolean removeAll();
    boolean removeTaskMap();
    boolean removeEpicMap();
    boolean removeSubTaskMap();
    List<SubTask> getSubTasksByEpic(Epic epic);
    List<Task> getHistory();

}
