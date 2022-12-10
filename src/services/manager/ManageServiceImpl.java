package services.manager;

import models.task.Epic;
import models.task.SubTask;
import models.task.Task;
import models.task.TaskStatus;
import services.status.StatusManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ManageServiceImpl implements ManageService, StatusManager {

    private HashMap<Integer,Task> taskRepository;

    public ManageServiceImpl() {
        taskRepository = new HashMap<>();
    }

    @Override
    public HashMap<Integer, Task> getAllTaskList() {
        if (!taskRepository.isEmpty()){
            return taskRepository;
        }
        System.out.println("Список задач пуст.");
        return new HashMap<>();
    }

    @Override
    public boolean removeAllTasks() {
        taskRepository.clear();
        if(taskRepository.isEmpty()){
            return true;
        }
        System.out.println("Что-то пошло не так.");
        return false;
    }

    @Override
    public Task getTaskById(Integer id) {
        if (taskRepository.containsKey(id)){
            return taskRepository.get(id);
        }
        System.out.printf("Задачи под номером %d нет.\n", id);
        return new Task();
    }

    @Override
    public Task addTask(Task task) {
        taskRepository.put(task.getId(),task);
        return taskRepository.get(task.getId());
    }

    @Override
    public Task addEpicTask(Epic task) {
        return null;
    }

    @Override
    public Task addSubTask(Epic epicTask, SubTask subTask) {
        return null;
    }

    @Override
    public Task updateTask(Task task) {
        //вытащить и проверить на класс
        Task tempTask = taskRepository.get(task.getId());
        if (tempTask != null) {
                Task newTask = tempTask;
                newTask.setId(task.getId());
                newTask.setTitle(task.getTitle());
                newTask.setDescription(task.getDescription());
                newTask.setStatus(task.getStatus());
                taskRepository.put(task.getId(),newTask);
                return taskRepository.get(task.getId());
        }
        System.out.printf("Задачи с id = %d нет.\n",task.getId());
        return new Task();
    }

    @Override
    public Epic updateTask(Epic task) {
        Epic tempTask = (Epic) taskRepository.get(task.getId());
        if (tempTask != null) {
            Epic newEpic = tempTask;
            newEpic.setId(task.getId());
            newEpic.setTitle(task.getTitle());
            newEpic.setDescription(task.getDescription());
            newEpic.setSubTasks(task.getSubTasks());
            newEpic.setStatus(getEpicStatus(task.getSubTasks()));
            taskRepository.put(task.getId(),newEpic);
            return (Epic) taskRepository.get(task.getId());
        }
        System.out.printf("Задачи с id = %d нет.\n",task.getId());
        return new Epic();
    }

    @Override
    public SubTask updateTask(SubTask task) {
        SubTask tempTask = (SubTask) taskRepository.get(task.getId());
        if (tempTask != null) {
            SubTask newSubtask = new SubTask();

            newSubtask.setId(task.getId());
            newSubtask.setTitle(task.getTitle());
            newSubtask.setDescription(task.getDescription());
            newSubtask.setIdEpic(task.getIdEpic());
            newSubtask.setNameEpic(task.getNameEpic());
            newSubtask.setStatus(task.getStatus());
            taskRepository.put(task.getId(),newSubtask);

            return (SubTask) taskRepository.get(task.getId());
        }
        System.out.printf("Задачи с id = %d нет.\n",task.getId());
        return new SubTask();
    }

    @Override
    public boolean removeTaskById(Integer id) {
        if (taskRepository.containsKey(id)){
            taskRepository.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public TaskStatus setEpicStatus(Epic epic) {
        Epic task = (Epic) taskRepository.get(epic.getId());
        List<SubTask> subTasks = task.getSubTasks();

        return getEpicStatus(subTasks);
    }

    public TaskStatus getEpicStatus(List<SubTask> subTasks){

        boolean isNew = subTasks.stream().allMatch(subTask -> subTask.getStatus().equals(TaskStatus.NEW));
        boolean isDone = subTasks.stream().allMatch(subTask -> subTask.getStatus().equals(TaskStatus.DONE));

        if (subTasks.isEmpty() || isNew){
            return TaskStatus.NEW;
        } else if (isDone) {
            return TaskStatus.DONE;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }
}
