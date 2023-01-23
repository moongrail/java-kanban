package services.manager;

import exceptions.ManagerSaveException;
import models.task.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import static models.task.TaskType.EPIC;
import static models.task.TaskType.TASK;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTasksManager(final File file) {
        super();
        if (file.exists()) {
            this.file = file;
        } else {
            throw new ManagerSaveException("Ошибка чтения файла: " + file);
        }
    }

    @Override
    public Task addTask(Task task) {
        save();
        return super.addTask(task);
    }

    @Override
    public Epic addEpic(Epic task) {
        save();
        return super.addEpic(task);
    }

    @Override
    public SubTask addSubTask(Integer idEpic, SubTask subTask) {
        save();
        return super.addSubTask(idEpic, subTask);
    }

    private void save() {
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic\n");
            HashMap<Integer, String> allTasksMap = new HashMap<>();
            HashMap<Integer, Task> tasksMap = super.getTaskRepository();
            for (Integer id : tasksMap.keySet()) {
                allTasksMap.put(id, toString(tasksMap.get(id)));
            }

            HashMap<Integer, Epic> epicsMap = super.getEpicRepository();
            for (Integer id : epicsMap.keySet()) {
                allTasksMap.put(id, toString(epicsMap.get(id)));
            }

            HashMap<Integer, SubTask> subtasks = super.getSubTaskRepository();
            for (Integer id : subtasks.keySet()) {
                allTasksMap.put(id, toString(subtasks.get(id)));
            }

            for (String value : allTasksMap.values()) {
                writer.write(String.format("%s\n", value));
            }
            writer.write("\n\n");
            writer.write(historyToString(this.getHistoryManager()));

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи файла.");
        }
    }

    private <T extends Task> String toString(T task) {
        try {
            if (task.getType() == TASK || task.getType() == EPIC) {
                return String.format("%d,%s,%s,%s,%s,", task.getId(), task.getType(), task.getTitle(), task.getStatus()
                        , task.getDescription());
            } else {
                SubTask tempSubTask = (SubTask) task;
                return String.format("%d,%s,%s,%s,%s,%d", tempSubTask.getId(), tempSubTask.getType(), tempSubTask.getTitle()
                        , tempSubTask.getStatus(), tempSubTask.getDescription(), tempSubTask.getIdEpic());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Переданный объект пуст" + e.getMessage());
        }
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        boolean firstLine = true;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            File fileToRecovered = Files.createFile(Path.of("resources", "recovered_history_" +
                    LocalDate.now() + UUID.randomUUID() + ".cvs")).toFile();
            FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(fileToRecovered);
            while (reader.ready()) {
                String line = reader.readLine();
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isEmpty() || line.isBlank()) {
                    continue;
                }
                if (checkHistoryLine(line)) {
                    recoverHistoryManager(fileBackedTasksManager, line);
                    continue;
                }
                String[] splitCurrentLine = line.split(",");
                TaskType type = TaskType.valueOf(splitCurrentLine[1]);
                switch (type) {
                    case TASK:
                        Task task = fromString(line);
                        fileBackedTasksManager.taskRepository.put(task.getId(), task);
                        break;
                    case EPIC:
                        Epic epic = fromString(line);
                        fileBackedTasksManager.epicRepository.put(epic.getId(), epic);
                        break;
                    case SUBTASK:
                        SubTask subTask = fromString(line);
                        fileBackedTasksManager.subTaskRepository.put(subTask.getId(), subTask);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + splitCurrentLine[1]);
                }
            }
            Epic updateEpic = null;
            for (SubTask entry : fileBackedTasksManager.subTaskRepository.values()) {
                updateEpic = fileBackedTasksManager.epicRepository.get(entry.getIdEpic());
                List<SubTask> subTasks = updateEpic.getSubTasks();
                subTasks.add(entry);
                updateEpic.setSubTasks(subTasks);
            }
            fileBackedTasksManager.updateEpic(updateEpic);
            return fileBackedTasksManager;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла: " + e.getMessage());
        }
    }

    private static void recoverHistoryManager(FileBackedTasksManager fileBackedTasksManager, String line) {
        List<Integer> historyIdGetTasks = historyFromString(line);
        HashMap<Integer, Task> allMap = fileBackedTasksManager.getAllMap();
        for (Integer historyId : historyIdGetTasks) {
            fileBackedTasksManager.historyManager.add(allMap.get(historyId));
        }
    }

    private static boolean checkHistoryLine(String line) {
        String[] split = line.split(",");
        for (String s : split) {
            if (!s.matches("\\d{1,}")) {
                return false;
            }
        }
        return true;
    }

    private static String historyToString(HistoryManager manager) {
        try {
            List<Task> history = manager.getHistory();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < history.size(); i++) {
                if (i != history.size() - 1) {
                    sb.append(history.get(i).getId()).append(",");
                } else {
                    sb.append(history.get(i).getId());
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Менеджер истории пуст: " + e.getMessage());
        }
    }

    private static List<Integer> historyFromString(String value) {
        try {
            String[] splitHistoryLine = value.split(",");
            List<Integer> historyTaskId = new ArrayList<>();
            for (String line : splitHistoryLine) {
                historyTaskId.add(Integer.valueOf(line));
            }
            Collections.reverse(historyTaskId);
            return historyTaskId;
        } catch (Exception e) {
            throw new IllegalArgumentException("Передана пустая строка: " + e.getMessage());
        }
    }

    public static <T extends Task> T fromString(String line) {
        try {
            String[] lines = line.split(",");
            Integer id = Integer.valueOf(lines[0]);
            TaskType type = TaskType.valueOf(lines[1]);
            String tittle = lines[2];
            TaskStatus status = TaskStatus.valueOf(lines[3]);
            String description = lines[4];
            switch (type) {
                case TASK:
                    return (T) new Task(id, type, tittle, status, description);
                case EPIC:
                    return (T) new Epic(id, type, tittle, status, description);
                case SUBTASK:
                    return (T) new SubTask(id, type, tittle, status, description, Integer.valueOf(lines[5]));
                default:
                    throw new IllegalArgumentException("Ошибка преобразования строки в объекта");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка преобразования строки в объект: " + e.getMessage());
        }

    }


    @Override
    public Task updateTask(Task task) {
        save();
        return super.updateTask(task);
    }

    @Override
    public Epic updateEpic(Epic updateEpic) {
        save();
        return super.updateEpic(updateEpic);
    }

    @Override
    public SubTask updateSubTask(SubTask task) {
        save();
        return super.updateSubTask(task);
    }

    @Override
    public void removeAll() {
        super.removeAll();
        save();
    }

    @Override
    public void removeTaskById(Integer id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpicById(Integer id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubTask(Integer id) {
        super.removeSubTask(id);
        save();
    }

    @Override
    public void removeTaskMap() {
        save();
        super.removeTaskMap();
    }

    @Override
    public void removeEpicMap() {
        super.removeEpicMap();
        save();
    }

    @Override
    public void removeSubTaskMap() {
        super.removeSubTaskMap();
        save();
    }

    @Override
    public Task getTaskById(Integer id) {
        save();
        return super.getTaskById(id);
    }

    @Override
    public Epic getEpicById(Integer id) {
        save();
        return super.getEpicById(id);
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        save();
        return super.getSubTaskById(id);
    }
}
