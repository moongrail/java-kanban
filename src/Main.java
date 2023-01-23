import exceptions.ManagerSaveException;
import models.task.*;
import services.manager.FileBackedTasksManager;
import services.manager.TaskManager;
import services.util.Managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        Path resources = Path.of("resources", "history.csv");

        TaskManager taskManager = getFileBackedTaskManagersAfterTestExistFile(resources);


        Epic firstEpic = new Epic(1, TaskType.EPIC, "Купить продукты", TaskStatus.NEW
                , "Сходить в магазин за продуктами");
        Epic secondEpic = new Epic(2, TaskType.EPIC, "Спорт", TaskStatus.NEW, "Побегать в парке.");

        SubTask firstSubTask = new SubTask(3, TaskType.SUBTASK, "Купить помидоры"
                , TaskStatus.NEW, "Найти свежие помидоры в магазине."
                , firstEpic.getId());
        SubTask secondSubTask = new SubTask(4, TaskType.SUBTASK, "Купить пиццу", TaskStatus.NEW
                , "Заказать пиццу в кафе"
                , firstEpic.getId());
        SubTask thirdSubTask = new SubTask(5, TaskType.SUBTASK, "Обман", TaskStatus.DONE
                , "Какой из тебя спортсмен.", firstEpic.getId());
        Task simpleTask = new Task(77, TaskType.TASK, "Просто задание", TaskStatus.DONE
                , "Просто задание и никак не связано с эпиком или подзадачей эпика.");

        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubTask(firstEpic.getId(), firstSubTask);
        taskManager.addSubTask(firstEpic.getId(), secondSubTask);
        taskManager.addSubTask(firstEpic.getId(), thirdSubTask);
        taskManager.addTask(simpleTask);

        taskManager.getTaskById(77);
        taskManager.getTaskById(77);
        taskManager.getTaskById(77);
        taskManager.getTaskById(77);
        taskManager.getEpicById(2);
        taskManager.getEpicById(1);
        taskManager.getSubTaskById(3);
        taskManager.getSubTaskById(4);
        taskManager.getSubTaskById(5);
        taskManager.getSubTaskById(3);


        System.out.println("###########################################");
        printHistory(taskManager.getHistory());
//        taskManager.removeSubTask(3);
//        taskManager.removeEpicById(1);

        FileBackedTasksManager recoveredFileBackedTasksManager = FileBackedTasksManager
                .loadFromFile(new File(String.valueOf(resources)));

        System.out.println("###########################################");
//        printHistory(taskManager.getHistory());
    }

    private static TaskManager getFileBackedTaskManagersAfterTestExistFile(Path resources) {
        File dir = Path.of("resources").toFile();
        if (!dir.exists()){
            dir.mkdir();
        }
        Path file = null;
        TaskManager taskManager = null;
        if (!resources.toFile().exists()) {
            try {
                file = Files.createFile(resources);
            } catch (IOException e) {
                throw new ManagerSaveException(e.getMessage());
            }
            taskManager = Managers.getDefaultFileBackedTasksManager(file.toFile());
        } else {
            taskManager = Managers.getDefaultFileBackedTasksManager(resources.toFile());
        }
        return taskManager;
    }

    private static void printHistory(List<Task> history) {
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("Строчка %d: %s\n", i + 1, history.get(i).getId());
        }
    }
}
