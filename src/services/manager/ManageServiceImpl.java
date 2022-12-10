package services.manager;

import models.task.Epic;
import models.task.SubTask;
import models.task.Task;
import models.task.TaskStatus;
import services.status.StatusManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ManageServiceImpl implements ManageService, StatusManager {

    private final HashMap<Integer, Task> taskRepository;

    public ManageServiceImpl() {
        taskRepository = new HashMap<>();
    }

    @Override
    public HashMap<Integer, Task> getAllTaskList() {
        if (!taskRepository.isEmpty()) {
            return taskRepository;
        }
        System.out.println("Список задач пуст.");
        return new HashMap<>();
    }

    @Override
    public boolean removeAllTasks() {
        taskRepository.clear();
        if (taskRepository.isEmpty()) {
            return true;
        }
        System.out.println("Что-то пошло не так.");
        return false;
    }

    @Override
    public Task getTaskById(Integer id) {
        if (taskRepository.containsKey(id)) {
            return taskRepository.get(id);
        }
        System.out.printf("Задачи под номером %d нет.\n", id);
        return new Task();
    }

    @Override
    public Task addTask(Task task) {
        taskRepository.put(task.getId(), task);
        return taskRepository.get(task.getId());
    }

    @Override
    public Task addTask(Epic task) {
        task.setStatus(getEpicStatus(task.getSubTasks()));
        taskRepository.put(task.getId(), task);
        return taskRepository.get(task.getId());
    }

    @Override
    public Task addSubTask(Integer idEpic, SubTask subTask) {
        taskRepository.put(subTask.getId(), subTask);
        Epic taskEpic = (Epic) taskRepository.get(idEpic);
        List<SubTask> subTasks = taskEpic.getSubTasks();
        subTasks.add(subTask);
        taskEpic.setStatus(getEpicStatus(taskEpic.getSubTasks()));
        taskRepository.put(taskEpic.getId(), taskEpic);
        taskEpic.setSubTasks(subTasks);
        return subTask;
    }

    @Override
    public Task updateTask(Task task) {
        Task tempTask = taskRepository.get(task.getId());
        if (tempTask != null) {
            tempTask.setId(task.getId());
            tempTask.setTitle(task.getTitle());
            tempTask.setDescription(task.getDescription());
            tempTask.setStatus(task.getStatus());
            taskRepository.put(task.getId(), tempTask);
            return taskRepository.get(task.getId());
        }
        System.out.printf("Задачи с id = %d нет.\n", task.getId());
        return new Task();
    }

    @Override
    public Epic updateTask(Epic task) {
        Epic tempTask = (Epic) taskRepository.get(task.getId());
        if (tempTask != null) {
            tempTask.setId(task.getId());
            tempTask.setTitle(task.getTitle());
            tempTask.setDescription(task.getDescription());
            tempTask.setSubTasks(task.getSubTasks());
            tempTask.setStatus(getEpicStatus(task.getSubTasks()));
            taskRepository.put(task.getId(), tempTask);
            return (Epic) taskRepository.get(task.getId());
        }
        System.out.printf("Задачи с id = %d нет.\n", task.getId());
        return new Epic();
    }

    @Override
    public SubTask updateTask(SubTask task) {
        SubTask oldSubTask = (SubTask) taskRepository.get(task.getId());

        if (oldSubTask != null) {
            SubTask newSubtask = new SubTask();

            newSubtask.setId(task.getId());
            newSubtask.setTitle(task.getTitle());
            newSubtask.setDescription(task.getDescription());
            newSubtask.setIdEpic(task.getIdEpic());
            newSubtask.setStatus(task.getStatus());

            Epic taskEpic = (Epic) taskRepository.get(newSubtask.getIdEpic());
            List<SubTask> subTasks = taskEpic.getSubTasks();
            subTasks.remove(oldSubTask);
            subTasks.add(newSubtask);
            taskEpic.setSubTasks(subTasks);
            taskEpic.setStatus(getEpicStatus(taskEpic.getSubTasks()));

            taskRepository.put(taskEpic.getId(), taskEpic);
            taskRepository.put(task.getId(), newSubtask);

            return (SubTask) taskRepository.get(task.getId());
        }
        System.out.printf("Задачи с id = %d нет.\n", task.getId());
        return new SubTask();
    }

    @Override
    public boolean removeTaskById(Integer id) {
        if (taskRepository.containsKey(id)) {
            boolean isSubTask = Arrays.stream(taskRepository.get(id).getClass()
                            .getDeclaredFields())
                            .anyMatch(field -> field.getName().equals("idEpic"));
            if (isSubTask) {
                SubTask task = (SubTask) taskRepository.get(id);
                Epic epicToChange = (Epic) taskRepository.get(task.getIdEpic());
                List<SubTask> subTasks = epicToChange.getSubTasks();
                subTasks.remove(task);
                epicToChange.setSubTasks(subTasks);
                taskRepository.put(epicToChange.getId(), epicToChange);
            }
            taskRepository.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public List<SubTask> getSubTasksByEpic(Epic epic) {
        Epic task = (Epic) taskRepository.get(epic.getId());
        List<SubTask> subTasks = task.getSubTasks();
        if (!subTasks.isEmpty()) {
            return subTasks;
        } else {
            System.out.println("Список подзадач пуст.");
            return new ArrayList<>();
        }
    }

    @Override
    public TaskStatus setEpicStatus(Epic epic) {
        Epic task = (Epic) taskRepository.get(epic.getId());
        List<SubTask> subTasks = task.getSubTasks();

        return getEpicStatus(subTasks);
    }

    public TaskStatus getEpicStatus(List<SubTask> subTasks) {

        boolean isNew = subTasks.stream().allMatch(subTask -> subTask.getStatus().equals(TaskStatus.NEW));
        boolean isDone = subTasks.stream().allMatch(subTask -> subTask.getStatus().equals(TaskStatus.DONE));

        if (subTasks.isEmpty() || isNew) {
            return TaskStatus.NEW;
        } else if (isDone) {
            return TaskStatus.DONE;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }
}
