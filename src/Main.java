import models.task.SubTask;
import models.task.Task;
import models.task.TaskStatus;
import services.manager.ManageService;
import services.manager.ManageServiceImpl;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        ManageService manageService = new ManageServiceImpl();
        Task task = new Task(1,"Купить продукты",
                "Сходить в магазин за продуктами", TaskStatus.NEW);
        Task task1 = new Task(2,"Бег",
                "Побегать в парке.", TaskStatus.NEW);
        SubTask subTask = new SubTask();
        manageService.addTask(subTask);
    }
}
