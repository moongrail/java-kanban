import models.task.Epic;
import models.task.SubTask;
import models.task.Task;
import models.task.TaskStatus;
import services.manager.ManageService;
import services.manager.ManageServiceImpl;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        ManageService manageService = new ManageServiceImpl();
        Task firstEpic = new Epic(1, "Купить продукты",
                "Сходить в магазин за продуктами");
        Task firstSubTask = new SubTask(3, "Купить помидоры", "Найти свежие помидоры в магазине."
                , TaskStatus.NEW, firstEpic.getId());
        Task secondSubTask = new SubTask(4, "Купить пиццу", "Заказать пиццу в кафе"
                , TaskStatus.NEW, firstEpic.getId());

        Task secondEpic = new Task(2, "Спорт",
                "Побегать в парке.", TaskStatus.NEW);
        Task thirdSubTask = new SubTask(5, "Обман", "Боже, иди домой, какой из тебя спортсмен.",
                TaskStatus.DONE, secondEpic.getId());

        manageService.addTask(firstEpic);
        manageService.addTask(secondEpic);
        manageService.addTask(firstSubTask);
        manageService.addTask(secondSubTask);
        manageService.addTask(thirdSubTask);

        //Распечатайте списки эпиков, задач и подзадач, через
        System.out.println(manageService.getAllTaskList());

        /* Измените статусы созданных объектов, распечатайте.
         Проверьте, что статус задачи и подзадачи сохранился,
         а статус эпика рассчитался по статусам подзадач.
         manageService.updateTask(secondSubTask)
         */
        Task updateSecondSubTask = new SubTask(4, "Купить пиццу", "Заказать пиццу в кафе"
                , TaskStatus.DONE, firstEpic.getId());

        manageService.updateTask(updateSecondSubTask);
        System.out.println(manageService.getTaskById(firstEpic.getId()));

        //И, наконец, попробуйте удалить одну из задач и один из эпиков.
        System.out.println(manageService.removeTaskById(firstEpic.getId())); //return true
        System.out.println(manageService.removeTaskById(thirdSubTask.getId())); //return true
        System.out.println(manageService.getAllTaskList());
    }
}
