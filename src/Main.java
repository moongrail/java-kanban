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

        Epic firstEpic = new Epic(1,"Купить продукты", "Сходить в магазин за продуктами");
        SubTask firstSubTask = new SubTask(3,"Купить помидоры", "Найти свежие помидоры в магазине."
                , TaskStatus.NEW, firstEpic.getId());
        SubTask secondSubTask = new SubTask(4,"Купить пиццу", "Заказать пиццу в кафе"
                , TaskStatus.NEW, firstEpic.getId());
        Epic secondEpic = new Epic(2,"Спорт", "Побегать в парке.");
        SubTask thirdSubTask = new SubTask(5,"Обман", "Боже, иди домой, какой из тебя спортсмен.",
                TaskStatus.DONE, secondEpic.getId());
        Task simpleTask = new Task(77,"Просто задание", "Просто задание и никак " +
                "не связано с эпиком или подзадачей эпика.", TaskStatus.DONE);

        manageService.addEpic(firstEpic);
        manageService.addEpic(secondEpic);
        manageService.addSubTask(firstEpic.getId(), firstSubTask);
        manageService.addSubTask(firstEpic.getId(), secondSubTask);
        manageService.addSubTask(secondEpic.getId(), thirdSubTask);
        manageService.addTask(simpleTask);

        //Распечатайте списки эпиков, задач и подзадач
        HashMap<Integer, Task> allTaskMap = manageService.getAllMap();
        allTaskMap.forEach((integer, task) -> System.out.println(integer + ": " + task));
        System.out.println("###########################################");
        /* Измените статусы созданных объектов, распечатайте.
         Проверьте, что статус задачи и подзадачи сохранился,
         а статус эпика рассчитался по статусам подзадач.
         manageService.updateTask(secondSubTask)
         */
        SubTask updateSecondSubTask = new SubTask(4, "Испечь пиццу", "Самому приготовить пиццу.",
                TaskStatus.DONE, firstEpic.getId());

        manageService.updateSubTask(updateSecondSubTask);
        System.out.println(manageService.getTaskById(firstEpic.getId()));
        System.out.println("###########################################");

        //И, наконец, попробуйте удалить одну из задач и один из эпиков.
        System.out.println("Удаление Эпика 1: " + manageService.removeEpicById(firstEpic.getId()));
        System.out.println("Удаление Сабтаска 5: " + manageService.removeSubTask(thirdSubTask.getId()));
        System.out.println("###########################################");
        HashMap<Integer, Task> finalAllTaskMap= manageService.getAllMap();
        finalAllTaskMap.forEach((integer, task) -> System.out.println(integer + ": " + task));

    }
}
