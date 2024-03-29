package services.manager;

import com.google.gson.annotations.SerializedName;
import models.task.Epic;
import models.task.SubTask;
import models.task.Task;
import services.manager.history.HistoryManager;
import services.status.StatusManager;
import services.status.StatusManagerImpl;
import services.util.Managers;
import services.util.TimeManagerUtil;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    @SerializedName("taskRepository")
    protected final HashMap<Integer, Task> taskRepository;
    @SerializedName("epicRepository")
    protected final HashMap<Integer, Epic> epicRepository;
    @SerializedName("subTaskRepository")
    protected final HashMap<Integer, SubTask> subTaskRepository;
    @SerializedName("historyManager")
    protected HistoryManager historyManager;

    private final StatusManager statusManager;

    public InMemoryTaskManager() {
        epicRepository = new HashMap<>();
        subTaskRepository = new HashMap<>();
        taskRepository = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
        statusManager = new StatusManagerImpl();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        HashMap<Integer, Task> allMap = getAllMap();
        PriorityQueue<Task> sorted = new PriorityQueue<>(Comparator.comparing(Task::getStartTime));
        List<Task> tasks = new ArrayList<>();

        for (Task task : allMap.values()) {
            if (task.getStartTime() == null) {
                tasks.add(task);
            } else {
                sorted.add(task);
            }
        }
        if (!sorted.isEmpty()) {
            List<Task> collect = sorted.stream()
                    .sorted(Comparator.comparing(Task::getStartTime))
                    .collect(Collectors.toList());

            collect.addAll(tasks);

            return collect;
        }
        return new ArrayList<>();
    }

    @Override
    public HashMap<Integer, Task> getAllMap() {
        HashMap<Integer, Task> allMap = new HashMap<>();

        allMap.putAll(taskRepository);
        allMap.putAll(epicRepository);
        allMap.putAll(subTaskRepository);

        if (!allMap.isEmpty()) {
            return allMap;
        }
        return new HashMap<>();
    }

    @Override
    public HashMap<Integer, Epic> getAllEpicMap() {
        if (!epicRepository.isEmpty()) {
            return epicRepository;
        }
        System.out.println("Список эпиков пуст.");
        return new HashMap<>();
    }

    @Override
    public HashMap<Integer, SubTask> getAllSubTaskMap() {
        if (!subTaskRepository.isEmpty()) {
            return subTaskRepository;
        }
        System.out.println("Список подзадач эпиков пуст.");
        return new HashMap<>();
    }

    @Override
    public HashMap<Integer, Task> getAllTaskMap() {
        if (!taskRepository.isEmpty()) {
            return taskRepository;
        }
        System.out.println("Список простых задач пуст.");
        return new HashMap<>();
    }

    @Override
    public void removeAll() {

        removeInHistoryFromRepository(taskRepository.keySet());
        removeInHistoryFromRepository(epicRepository.keySet());
        removeInHistoryFromRepository(subTaskRepository.keySet());

        taskRepository.clear();
        epicRepository.clear();
        subTaskRepository.clear();

        if (!taskRepository.isEmpty() && !epicRepository.isEmpty() && !subTaskRepository.isEmpty()) {
            System.out.println("Что-то пошло не так.");
        }
    }

    @Override
    public Task getTaskById(Integer id) {
        if (taskRepository.containsKey(id)) {
            Task task = taskRepository.get(id);
            historyManager.add(task);
            return task;
        }
        printErrorIdTask("Задачи под номером %d нет.\n", id);
        return null;
    }

    @Override
    public Epic getEpicById(Integer id) {
        if (epicRepository.containsKey(id)) {
            Epic epic = epicRepository.get(id);
            historyManager.add(epic);
            return epic;
        }
        printErrorIdTask("Задачи под номером %d нет.\n", id);
        return null;
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        if (subTaskRepository.containsKey(id)) {
            SubTask subTask = subTaskRepository.get(id);
            historyManager.add(subTask);
            return subTask;
        }
        printErrorIdTask("Задачи под номером %d нет.\n", id);
        return null;
    }

    @Override
    public void addTask(Task task) {
        if (task.getStartTime() == null) {
            taskRepository.put(task.getId(), task);
        } else if (TimeManagerUtil.checkSameStartAndProgressTimeTasks(task, getAllMap())) {
            taskRepository.put(task.getId(), task);
        } else {
            System.out.println("Время выполнения таска совпадают с другими задачами.");
        }
    }

    @Override
    public void addEpic(Epic task) {
        if (task.getStartTime() == null) {
            task.setStatus(statusManager.getEpicStatus(task.getSubTasks()));
            epicRepository.put(task.getId(), task);
        } else if (TimeManagerUtil.checkSameStartAndProgressTimeTasks(task, getAllMap())) {
            task.setStatus(statusManager.getEpicStatus(task.getSubTasks()));
            epicRepository.put(task.getId(), task);
        } else {
            System.out.println("Время выполнения таска совпадают с другими задачами.");
        }
    }

    @Override
    public void addSubTask(Integer idEpic, SubTask subTask) {
        if (TimeManagerUtil.checkSameStartAndProgressTimeTasks(subTask, getAllMap())) {
            subTaskRepository.put(subTask.getId(), subTask);

            Epic taskEpic = epicRepository.get(idEpic);
            if (taskEpic != null) {
                List<SubTask> subTasks = taskEpic.getSubTasks();
                subTasks.add(subTask);
                taskEpic.setStatus(statusManager.getEpicStatus(taskEpic.getSubTasks()));
                taskEpic.setSubTasks(subTasks);
                taskEpic.setStartTime(TimeManagerUtil.getStartTimeForEpic(subTasks));
                taskEpic.setEndTime(TimeManagerUtil.getEndTimeForEpic(subTasks));
                epicRepository.put(taskEpic.getId(), taskEpic);
            }
        } else {
            System.out.println("Время выполнения таска совпадают с другими задачами.");
        }
    }

    @Override
    public void updateTask(Task task) {
        if (TimeManagerUtil.checkSameStartAndProgressTimeTasks(task, getAllMap())) {
            Task tempTask = taskRepository.get(task.getId());

            if (tempTask != null) {
                tempTask.setId(task.getId());
                tempTask.setTitle(task.getTitle());
                tempTask.setDescription(task.getDescription());
                tempTask.setStatus(task.getStatus());
                if (task.getStartTime() != null && task.getDuration() != null) {
                    tempTask.setDuration(task.getDuration());
                    tempTask.setStartTime(task.getStartTime());
                    tempTask.setEndTime(task.getStartTime().plus(task.getDuration()));
                }
                taskRepository.put(task.getId(), tempTask);
            } else {
                printErrorIdTask("Задачи с id = %d нет.\n", task.getId());
            }
        } else {
            System.out.println("Время выполнения таска совпадают с другими задачами.");
        }
    }

    @Override
    public void updateEpic(Epic updateEpic) {
        if (TimeManagerUtil.checkSameStartAndProgressTimeTasks(updateEpic, getAllMap())) {
            Epic tempEpic = epicRepository.get(updateEpic.getId());

            if (tempEpic != null) {
                tempEpic.setId(updateEpic.getId());
                tempEpic.setTitle(updateEpic.getTitle());
                tempEpic.setDescription(updateEpic.getDescription());
                tempEpic.setSubTasks(updateEpic.getSubTasks());
                tempEpic.setStatus(statusManager.getEpicStatus(updateEpic.getSubTasks()));

                if (updateEpic.getStartTime() != null && updateEpic.getDuration() != null) {
                    tempEpic.setDuration(updateEpic.getDuration());
                    tempEpic.setStartTime(updateEpic.getStartTime());
                    tempEpic.setEndTime(updateEpic.getStartTime().plus(updateEpic.getDuration()));
                }

                epicRepository.put(updateEpic.getId(), tempEpic);
            } else {
                printErrorIdTask("Задачи с id = %d нет.\n", updateEpic.getId());
            }
        } else {
            System.out.println("Время выполнения таска совпадают с другими задачами.");
        }
    }

    @Override
    public void updateSubTask(SubTask task) {
        if (TimeManagerUtil.checkSameStartAndProgressTimeTasks(task, getAllMap())) {
            SubTask oldSubTask = subTaskRepository.get(task.getId());

            if (oldSubTask != null) {
                SubTask newSubtask = new SubTask();

                newSubtask.setId(task.getId());
                newSubtask.setTitle(task.getTitle());
                newSubtask.setDescription(task.getDescription());
                newSubtask.setIdEpic(task.getIdEpic());
                newSubtask.setStatus(task.getStatus());
                if (task.getStartTime() != null && task.getDuration() != null) {
                    newSubtask.setDuration(task.getDuration());
                    newSubtask.setStartTime(task.getStartTime());
                    newSubtask.setEndTime(task.getStartTime().plus(task.getDuration()));
                }
                Epic taskEpic = epicRepository.get(newSubtask.getIdEpic());
                List<SubTask> subTasks = taskEpic.getSubTasks();
                subTasks.remove(oldSubTask);
                subTasks.add(newSubtask);
                taskEpic.setSubTasks(subTasks);
                taskEpic.setStatus(statusManager.getEpicStatus(taskEpic.getSubTasks()));

                if (subTasks.stream().allMatch(subTask -> subTask.getStartTime() != null)) {
                    taskEpic.setStartTime(TimeManagerUtil.getStartTimeForEpic(subTasks));
                    taskEpic.setEndTime(TimeManagerUtil.getEndTimeForEpic(subTasks));
                }
                epicRepository.put(taskEpic.getId(), taskEpic);
                subTaskRepository.put(task.getId(), newSubtask);

            } else {
                printErrorIdTask("Задачи с id = %d нет.\n", task.getId());
            }
        } else {
            System.out.println("Время выполнения таска совпадают с другими задачами.");
        }
    }

    @Override
    public void removeTaskById(Integer id) {
        if (taskRepository.containsKey(id)) {
            historyManager.remove(id);
            taskRepository.remove(id);
        } else {
            printErrorIdTask("Задачи с id = %d нет.\n", id);
        }
    }

    @Override
    public void removeEpicById(Integer id) {
        if (epicRepository.containsKey(id)) {
            Epic task = epicRepository.get(id);
            for (int i = 0; i < task.getSubTasks().size(); i++) {
                historyManager.remove(task.getSubTasks().get(i).getId());
                subTaskRepository.remove(task.getSubTasks().get(i).getId());
            }
            historyManager.remove(id);
            epicRepository.remove(id);

        } else {
            printErrorIdTask("Задачи с id = %d нет.\n", id);
        }
    }

    @Override
    public void removeSubTask(Integer id) {
        if (subTaskRepository.containsKey(id)) {
            historyManager.remove(id);
            removeSubtaskInEpic(id);
            subTaskRepository.remove(id);

        } else {
            printErrorIdTask("Задачи с id = %d нет.\n", id);
        }
    }


    private void removeSubtaskInEpic(Integer id) {
        SubTask subTask = subTaskRepository.get(id);
        Epic epicToChange = epicRepository.get(subTask.getIdEpic());
        if (epicToChange != null) {
            List<SubTask> subTasks = epicToChange.getSubTasks();
            subTasks.remove(subTask);
            epicToChange.setSubTasks(subTasks);
            epicToChange.setStatus(statusManager.getEpicStatus(subTasks));
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
            return new ArrayList<>();
        }
    }

    @Override
    public void removeTaskMap() {
        removeInHistoryFromRepository(taskRepository.keySet());
        taskRepository.clear();
        if (!taskRepository.isEmpty()) {
            System.out.println("Что-то пошло не так.");
        }
    }

    @Override
    public void removeEpicMap() {
        removeInHistoryFromRepository(epicRepository.keySet());
        removeInHistoryFromRepository(subTaskRepository.keySet());
        epicRepository.clear();
        subTaskRepository.clear();
        if (!taskRepository.isEmpty() && !subTaskRepository.isEmpty()) {
            System.out.println("Что-то пошло не так.");
        }
    }

    private void removeInHistoryFromRepository(Set<Integer> subTaskRepository) {
        for (Integer taskId : subTaskRepository) {
            historyManager.remove(taskId);
        }
    }

    @Override
    public void removeSubTaskMap() {
        for (Epic epic : epicRepository.values()) {
            epic.setSubTasks(new ArrayList<>());
        }
        subTaskRepository.clear();
        if (!subTaskRepository.isEmpty()) {
            System.out.println("Что-то пошло не так.");
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = historyManager.getHistory();
        if (!history.isEmpty()) {
            return history;
        }
        System.out.println("Список истории пуст");
        return new ArrayList<>();
    }

    private void printErrorIdTask(String format, Integer id) {
        System.out.printf(format, id);
    }

    public HashMap<Integer, Task> getTaskRepository() {
        return taskRepository;
    }

    public HashMap<Integer, Epic> getEpicRepository() {
        return epicRepository;
    }

    public HashMap<Integer, SubTask> getSubTaskRepository() {
        return subTaskRepository;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public StatusManager getStatusManager() {
        return statusManager;
    }
}
