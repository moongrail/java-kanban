import models.task.Epic;
import models.task.SubTask;
import models.task.Task;
import models.task.TaskStatus;
import services.manager.TaskManager;
import services.util.Managers;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = Managers.getDefaultTaskManager();

        Epic firstEpic = new Epic(1, "Купить продукты", "Сходить в магазин за продуктами");
        SubTask firstSubTask = new SubTask(3, "Купить помидоры", "Найти свежие помидоры в магазине."
                , TaskStatus.NEW, firstEpic.getId());
        SubTask secondSubTask = new SubTask(4, "Купить пиццу", "Заказать пиццу в кафе"
                , TaskStatus.NEW, firstEpic.getId());
        Epic secondEpic = new Epic(2, "Спорт", "Побегать в парке.");
        SubTask thirdSubTask = new SubTask(5, "Обман", "Боже, иди домой, какой из тебя спортсмен.",
                TaskStatus.DONE, secondEpic.getId());
        Task simpleTask = new Task(77, "Просто задание", "Просто задание и никак " +
                "не связано с эпиком или подзадачей эпика.", TaskStatus.DONE);

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
        taskManager.removeEpicById(1);
        System.out.println("###########################################");
        printHistory(taskManager.getHistory());
    }

    private static void printHistory(List<Task> history) {
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("Строчка %d: %s\n", i + 1, history.get(i).getId());
        }
    }
}
