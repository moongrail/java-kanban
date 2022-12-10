import models.task.Epic;
import models.task.SubTask;
import models.task.Task;
import models.task.TaskStatus;
import services.manager.ManageService;
import services.manager.ManageServiceImpl;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        ManageService manageService = new ManageServiceImpl();
        Task firstEpic = new Epic(1, "Купить продукты",
                "Сходить в магазин за продуктами");
        SubTask firstSubTask = new SubTask(3, "Купить помидоры", "Найти свежие помидоры в магазине."
                , TaskStatus.NEW, firstEpic.getId());
        SubTask secondSubTask = new SubTask(4, "Купить пиццу", "Заказать пиццу в кафе"
                , TaskStatus.NEW, firstEpic.getId());

        Task secondEpic = new Epic(2, "Спорт",
                "Побегать в парке.");
        SubTask thirdSubTask = new SubTask(5, "Обман", "Боже, иди домой, какой из тебя спортсмен.",
                TaskStatus.DONE, secondEpic.getId());

        manageService.addTask(firstEpic);
        manageService.addTask(secondEpic);
        manageService.addSubTask(firstEpic.getId(), firstSubTask);
        manageService.addSubTask(firstEpic.getId(), secondSubTask);
        manageService.addSubTask(secondEpic.getId(), thirdSubTask);

        //Распечатайте списки эпиков, задач и подзадач
        HashMap<Integer, Task> allTaskList = manageService.getAllTaskList();
        allTaskList.forEach((integer, task) -> System.out.println(integer + ": " + task));
        System.out.println("###########################################");

        /* Измените статусы созданных объектов, распечатайте.
         Проверьте, что статус задачи и подзадачи сохранился,
         а статус эпика рассчитался по статусам подзадач.
         manageService.updateTask(secondSubTask)
         */
        SubTask updateSecondSubTask = new SubTask(4, "Испечь пиццу", "Самому приготовить пиццу в кафе"
                , TaskStatus.DONE, firstEpic.getId());

        manageService.updateTask(updateSecondSubTask);
        System.out.println(manageService.getTaskById(firstEpic.getId()));
        System.out.println("###########################################");

        //И, наконец, попробуйте удалить одну из задач и один из эпиков.
        System.out.println("Удаление 1: " + manageService.removeTaskById(firstEpic.getId())); //return true
        System.out.println("Удаление 2: " + manageService.removeTaskById(thirdSubTask.getId())); //return true
        System.out.println("###########################################");
        HashMap<Integer, Task> finalAllTaskList = manageService.getAllTaskList();
        finalAllTaskList.forEach((integer, task) -> System.out.println(integer + ": " + task));

    }
}
