package services.manager;

import models.task.Epic;
import models.task.SubTask;
import models.task.Task;
import models.task.TaskStatus;
import services.status.StatusManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ManageServiceImpl implements ManageService, StatusManager {

    private final HashMap<Integer, Task> taskRepository;
    private final HashMap<Integer, Epic> epicRepository;
    private final HashMap<Integer, SubTask> subTaskRepository;

    public ManageServiceImpl() {
        epicRepository = new HashMap<>();
        subTaskRepository = new HashMap<>();
        taskRepository = new HashMap<>();
    }

    @Override
    public HashMap<Integer, Task> getAllMap() {
        HashMap<Integer, Task> allMap = new HashMap<>();

        allMap.putAll(epicRepository);
        allMap.putAll(subTaskRepository);
        allMap.putAll(taskRepository);

        if (!allMap.isEmpty()) {
            return allMap;
        }
        System.out.println("Список задач пуст.");
        return null;
    }

    @Override
    public HashMap<Integer, Epic> getAllEpicMap() {
        if (!epicRepository.isEmpty()) {
            return epicRepository;
        }
        System.out.println("Список эпиков пуст.");
        return null;
    }

    @Override
    public HashMap<Integer, SubTask> getAllSubTaskMap() {
        if (!subTaskRepository.isEmpty()) {
            return subTaskRepository;
        }
        System.out.println("Список подзадач эпиков пуст.");
        return null;
    }

    @Override
    public HashMap<Integer, Task> getAllTaskMap() {
        if (!taskRepository.isEmpty()) {
            return taskRepository;
        }
        System.out.println("Список простых задач пуст.");
        return null;
    }

    @Override
    public boolean removeAll() {
        taskRepository.clear();
        epicRepository.clear();
        subTaskRepository.clear();

        if (taskRepository.isEmpty() && epicRepository.isEmpty() && subTaskRepository.isEmpty()) {
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
        printErrorIdTask("Задачи под номером %d нет.\n", id);
        return null;
    }

    @Override
    public Epic getEpicById(Integer id) {
        if (epicRepository.containsKey(id)) {
            return epicRepository.get(id);
        }
        printErrorIdTask("Задачи под номером %d нет.\n", id);
        return null;
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        if (subTaskRepository.containsKey(id)) {
            return subTaskRepository.get(id);
        }
        printErrorIdTask("Задачи под номером %d нет.\n", id);
        return null;
    }

    @Override
    public Task addTask(Task task) {
        taskRepository.put(task.getId(), task);
        return taskRepository.get(task.getId());
    }

    @Override
    public Epic addEpic(Epic task) {
        task.setStatus(getEpicStatus(task.getSubTasks()));
        epicRepository.put(task.getId(), task);
        return epicRepository.get(task.getId());
    }

    @Override
    public SubTask addSubTask(Integer idEpic, SubTask subTask) {
        subTaskRepository.put(subTask.getId(), subTask);

        Epic taskEpic = epicRepository.get(idEpic);
        if (taskEpic != null) {
            List<SubTask> subTasks = taskEpic.getSubTasks();
            subTasks.add(subTask);
            taskEpic.setStatus(getEpicStatus(taskEpic.getSubTasks()));
            taskEpic.setSubTasks(subTasks);
            epicRepository.put(taskEpic.getId(), taskEpic);
        }
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
        printErrorIdTask("Задачи с id = %d нет.\n", task.getId());
        return null;
    }

    @Override
    public Epic updateEpic(Epic updateEpic) {
        Epic tempEpic = epicRepository.get(updateEpic.getId());

        if (tempEpic != null) {
            tempEpic.setId(updateEpic.getId());
            tempEpic.setTitle(updateEpic.getTitle());
            tempEpic.setDescription(updateEpic.getDescription());
            tempEpic.setSubTasks(updateEpic.getSubTasks());
            tempEpic.setStatus(getEpicStatus(updateEpic.getSubTasks()));

            epicRepository.put(updateEpic.getId(), tempEpic);

            return epicRepository.get(updateEpic.getId());
        }
        printErrorIdTask("Задачи с id = %d нет.\n", updateEpic.getId());
        return null;
    }

    @Override
    public SubTask updateSubTask(SubTask task) {
        SubTask oldSubTask = subTaskRepository.get(task.getId());

        if (oldSubTask != null) {
            SubTask newSubtask = new SubTask();

            newSubtask.setId(task.getId());
            newSubtask.setTitle(task.getTitle());
            newSubtask.setDescription(task.getDescription());
            newSubtask.setIdEpic(task.getIdEpic());
            newSubtask.setStatus(task.getStatus());

            Epic taskEpic = epicRepository.get(newSubtask.getIdEpic());
            List<SubTask> subTasks = taskEpic.getSubTasks();
            subTasks.remove(oldSubTask);
            subTasks.add(newSubtask);
            taskEpic.setSubTasks(subTasks);
            taskEpic.setStatus(getEpicStatus(taskEpic.getSubTasks()));

            epicRepository.put(taskEpic.getId(), taskEpic);
            subTaskRepository.put(task.getId(), newSubtask);

            return subTaskRepository.get(task.getId());
        }
        printErrorIdTask("Задачи с id = %d нет.\n", task.getId());
        return null;
    }

    @Override
    public boolean removeTaskById(Integer id) {
        if (taskRepository.containsKey(id)) {
            taskRepository.remove(id);
            return true;
        }
        printErrorIdTask("Задачи с id = %d нет.\n", id);
        return false;
    }

    @Override
    public boolean removeEpicById(Integer id) {
        if (epicRepository.containsKey(id)) {
            Epic task = epicRepository.get(id);
            for (int i = 0; i < task.getSubTasks().size(); i++) {
                subTaskRepository.remove(task.getSubTasks().get(i).getId());
            }
            epicRepository.remove(id);
            return true;
        }
        printErrorIdTask("Задачи с id = %d нет.\n", id);
        return false;
    }

    @Override
    public boolean removeSubTask(Integer id) {
        if (subTaskRepository.containsKey(id)) {
            removeSubtaskInEpic(id);
            subTaskRepository.remove(id);
            return true;
        }
        printErrorIdTask("Задачи с id = %d нет.\n", id);
        return false;
    }


    private void removeSubtaskInEpic(Integer id) {
        SubTask subTask = subTaskRepository.get(id);
        Epic epicToChange = epicRepository.get(subTask.getIdEpic());
        if (epicToChange != null) {
            List<SubTask> subTasks = epicToChange.getSubTasks();
            subTasks.remove(subTask);
            epicToChange.setSubTasks(subTasks);
            epicToChange.setStatus(getEpicStatus(subTasks));
            epicRepository.put(epicToChange.getId(), epicToChange);
        } else {
            printErrorIdTask("Эпика с id = %d нет.\n", id);
        }
    }

    @Override
    public List<SubTask> getSubTasksByEpic(Epic epic) {
        Epic task = epicRepository.get(epic.getId());
        List<SubTask> subTasks = task.getSubTasks();
        if (!subTasks.isEmpty()) {
            return subTasks;
        } else {
            System.out.println("Список подзадач пуст.");
            return null;
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

    @Override
    public boolean removeTaskMap() {
        taskRepository.clear();
        if (taskRepository.isEmpty()) {
            return true;
        }
        System.out.println("Что-то пошло не так.");
        return false;
    }

    @Override
    public boolean removeEpicMap() {
        epicRepository.clear();
        subTaskRepository.clear();
        if (taskRepository.isEmpty() && subTaskRepository.isEmpty()) {
            return true;
        }
        System.out.println("Что-то пошло не так.");
        return false;
    }

    @Override
    public boolean removeSubTaskMap() {
        for (Epic epic : epicRepository.values()) {
            epic.setSubTasks(new ArrayList<>());
        }
        subTaskRepository.clear();
        if (subTaskRepository.isEmpty()) {
            return true;
        }
        System.out.println("Что-то пошло не так.");
        return false;
    }

    private void printErrorIdTask(String format, Integer id) {
        System.out.printf(format, id);
    }
}
