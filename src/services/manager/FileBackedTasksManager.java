package services.manager;

import handlers.ManagerSaveException;
import models.task.*;

import java.io.*;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private final File filename;

    public FileBackedTasksManager(final File filename) {
        super();
        this.filename = filename;
    }

    @Override
    public Task addTask(Task task) {
        save(task);
        return super.addTask(task);
    }

    @Override
    public Epic addEpic(Epic task) {
        save(task);
        return super.addEpic(task);
    }

    @Override
    public SubTask addSubTask(Integer idEpic, SubTask subTask) {
        save(subTask);
        return super.addSubTask(idEpic, subTask);
    }

    private <T extends Task> void save(T task) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(toString(task));
            writer.newLine();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения: " + e.getMessage());
        }
    }

    private <T extends Task> String toString(T task) {
        if (task.getType() == TaskType.TASK || task.getType() == TaskType.EPIC) {
            return String.format("%d,%s,%s,%s,%s,", task.getId(), task.getType(), task.getTitle(), task.getStatus()
                    , task.getDescription());
        } else {
            SubTask tempSubTask = (SubTask) task;
            return String.format("%d,%s,%s,%s,%s,%d", tempSubTask.getId(), tempSubTask.getType(), tempSubTask.getTitle()
                    , tempSubTask.getStatus(), tempSubTask.getDescription(), tempSubTask.getIdEpic());
        }
    }

   public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.ready()) {
                String[] splitCurrentLine =  reader.readLine().split(",");
                if (splitCurrentLine[1].equals(TaskType.TASK)){
                    Task task = fromString(splitCurrentLine);
                    fileBackedTasksManager.addTask(task);
                }else if (splitCurrentLine[1].equals(TaskType.SUBTASK)){
                    SubTask subTask = fromString(splitCurrentLine);
                    fileBackedTasksManager.addSubTask(subTask.getIdEpic(),subTask);
                }else {
                    Epic epic = fromString(splitCurrentLine);
                    fileBackedTasksManager.addEpic(epic);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла: " + e.getMessage());
        }
        return fileBackedTasksManager;
    }

//    static String historyToString(HistoryManager manager) {
//
//    }
//
//    static List<Integer> historyFromString(String value) {
//
//    }

     static <T extends Task> T fromString(String[] lines) {
        if (lines[1].equals(TaskType.TASK)) {
            Task newTask = new Task();
            newTask.setId(Integer.valueOf(lines[0]));
            newTask.setType(TaskType.valueOf(lines[1]));
            newTask.setTitle(lines[2]);
            newTask.setStatus(TaskStatus.valueOf(lines[3]));
            newTask.setDescription(lines[4]);
            return (T) newTask;
        } else if (lines[1].equals(TaskType.SUBTASK)) {
            SubTask newSubTask = new SubTask();
            newSubTask.setId(Integer.valueOf(lines[0]));
            newSubTask.setType(TaskType.valueOf(lines[1]));
            newSubTask.setTitle(lines[2]);
            newSubTask.setStatus(TaskStatus.valueOf(lines[3]));
            newSubTask.setDescription(lines[4]);
            newSubTask.setIdEpic(Integer.valueOf(lines[5]));
            return (T) newSubTask;
        } else {
            Epic newEpic = new Epic();
            newEpic.setId(Integer.valueOf(lines[0]));
            newEpic.setType(TaskType.valueOf(lines[1]));
            newEpic.setTitle(lines[2]);
            newEpic.setStatus(TaskStatus.valueOf(lines[3]));
            newEpic.setDescription(lines[4]);
            return (T) newEpic;
        }
    }
}
