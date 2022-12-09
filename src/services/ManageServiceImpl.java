package services;

import models.task.Task;

import java.util.HashMap;

public class ManageServiceImpl implements ManageService {

    private static int counterTask = 1;
    private  final HashMap<Integer,Task> taskRepository = new HashMap<>();


    @Override
    public HashMap<Integer, Task> getAllTaskList() {
        if (!taskRepository.isEmpty()){
            return taskRepository;
        }
        System.out.println("Список задач пуст.");
        return new HashMap<>();
    }

    @Override
    public boolean removeAllTasks() {
        taskRepository.clear();
        if(taskRepository.isEmpty()){
            counterTask = 1;
            return true;
        }
        System.out.println("Что-то пошло не так.");
        return false;
    }

    @Override
    public Task getTaskById(Integer id) {
        if (taskRepository.containsKey(id)){
            return taskRepository.get(id);
        }
        System.out.printf("Задачи под номером %d нет.", id);
        return new Task();
    }

    @Override
    public Task createTask(Task task) {
        taskRepository.put(counterTask,task);
        counterTask++;
        return taskRepository.get(counterTask-1);
    }

    @Override
    public Task updateTask(Integer id) {
        //вытащить и проверить на класс
//        if()
        return null;
    }

    @Override
    public boolean removeTaskById(Integer id) {
        if (taskRepository.containsKey(id)){
            taskRepository.remove(id);
            return true;
        }
        return false;
    }
}
