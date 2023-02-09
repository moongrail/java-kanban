package test.services.manager;

import models.task.*;
import org.junit.jupiter.api.Test;
import services.manager.TaskManager;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @Test
    public void allRepositoriesIsBlank() {
        assertTrue(taskManager.getAllMap().isEmpty());
    }

    @Test
    public void whenAddTaskRepositoryIncreased() {
        taskManager.addTask(new Task(1, TaskType.TASK, "test", TaskStatus.NEW, "test"));
        assertEquals(1, taskManager.getAllTaskMap().size());
    }

    @Test
    public void whenAddEpicRepositoryIncreased() {
        taskManager.addEpic(new Epic(1, TaskType.EPIC, "epic", TaskStatus.NEW, "epic"));
        assertEquals(1, taskManager.getAllEpicMap().size());
    }

    @Test
    public void whenAddSubTaskRepositoryIncreasedAndSubtaskAddedInEpic() {
        taskManager.addEpic(new Epic(1, TaskType.EPIC, "epic", TaskStatus.NEW, "epic"));
        SubTask subTask = new SubTask(2, TaskType.SUBTASK, "test", TaskStatus.NEW, "test", 1);
        taskManager.addSubTask(1, subTask);
        assertEquals(1, taskManager.getAllSubTaskMap().size());
        boolean result = taskManager.getAllEpicMap().values()
                .stream()
                .anyMatch(epic -> epic.getSubTasks().contains(subTask));

        assertTrue(result);
    }

    @Test
    public void allMapHaveSize10() {
        addDataInRepositoriesSize10();
        assertEquals(10, taskManager.getAllMap().size());
    }

    @Test
    public void whenEpicMapHaveSize3() {
        addDataInRepositoriesSize10();
        assertEquals(3, taskManager.getAllEpicMap().size());
    }

    @Test
    public void whenSubtaskMapHaveSize5() {
        addDataInRepositoriesSize10();
        assertEquals(5, taskManager.getAllSubTaskMap().size());
    }

    @Test
    public void whenTaskMapHaveSize2() {
        addDataInRepositoriesSize10();
        assertEquals(2, taskManager.getAllTaskMap().size());
    }

    @Test
    public void getTaskByIdWhenExisted() {
        addDataInRepositoriesSize10();
        Task task = taskManager.getTaskById(1);
        assertEquals(1, task.getId());
    }

    @Test
    public void getTaskByIdWhenNotExisted() {
        Task task = taskManager.getTaskById(1);
        assertNull(task);
    }

    @Test
    public void getEpicByIdWhenExisted() {
        addDataInRepositoriesSize10();
        Epic task = taskManager.getEpicById(3);
        assertEquals(3, task.getId());
    }

    @Test
    public void getEpicByIdWhenNotExisted() {
        Epic task = taskManager.getEpicById(3);
        assertNull(task);
    }

    @Test
    public void getSubTaskByIdWhenExisted() {
        addDataInRepositoriesSize10();
        SubTask task = taskManager.getSubTaskById(5);
        assertEquals(5, task.getId());
    }

    @Test
    public void getSubTaskByIdWhenNotExisted() {
        SubTask task = taskManager.getSubTaskById(5);
        assertNull(task);
    }

    @Test
    public void testStatusEpicAfterAddedSubTask() {
        addDataInRepositoriesSize10();
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(3).getStatus());
        assertEquals(TaskStatus.DONE, taskManager.getEpicById(9).getStatus());
        assertEquals(TaskStatus.NEW, taskManager.getEpicById(4).getStatus());
    }

    @Test
    public void testUpdateTask() {
        addDataInRepositoriesSize10();
        Task task = taskManager.getTaskById(1);
        task.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task);
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    public void testUpdateWhenTaskNotExisted() {
        Task task = taskManager.getTaskById(1);
        assertThrows(NullPointerException.class, () -> {
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task);
        });
        assertNull(task);
    }

    @Test
    public void testUpdateEpic() {
        addDataInRepositoriesSize10();
        Epic task = taskManager.getEpicById(3);
        task.setTitle("UPDATE");
        taskManager.updateEpic(task);
        assertEquals("UPDATE", task.getTitle());
    }

    @Test
    public void testUpdateWhenEpicNotExisted() {
        Epic task = taskManager.getEpicById(1);
        assertThrows(NullPointerException.class, () -> {
            task.setStatus(TaskStatus.DONE);
            taskManager.updateEpic(task);
        });
        assertNull(task);
    }

    @Test
    public void testUpdateSubtask() {
        addDataInRepositoriesSize10();
        SubTask task = taskManager.getSubTaskById(10);
        task.setStatus(TaskStatus.NEW);
        taskManager.updateSubTask(task);
        assertEquals(TaskStatus.NEW, task.getStatus());
        Epic epic = taskManager.getEpicById(9);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    public void testUpdateWhenSubtaskNotExisted() {
        SubTask task = taskManager.getSubTaskById(1);
        assertThrows(NullPointerException.class, () -> {
            task.setStatus(TaskStatus.DONE);
            taskManager.updateSubTask(task);
        });
        assertNull(task);
    }

    @Test
    public void testRemoveTaskIfExists() {
        addDataInRepositoriesSize10();
        taskManager.removeTaskById(1);
        assertFalse(taskManager.getAllTaskMap().containsKey(1));
    }

    @Test
    public void testRemoveEpicIfExists() {
        addDataInRepositoriesSize10();
        taskManager.removeEpicById(9);
        assertFalse(taskManager.getAllEpicMap().containsKey(9));
        assertFalse(taskManager.getAllSubTaskMap().values().stream()
                .anyMatch(subTask -> subTask.getIdEpic().equals(9)));
    }

    @Test
    public void testRemoveSubTaskIfExists() {
        addDataInRepositoriesSize10();
        int idEpic = taskManager.getSubTaskById(10).getIdEpic();
        taskManager.removeSubTask(10);

        List<SubTask> subTasks = taskManager.getEpicById(idEpic).getSubTasks();

        assertFalse(taskManager.getAllSubTaskMap().containsKey(10));
        assertFalse(subTasks.stream().anyMatch(subTask -> subTask.getId().equals(10)));
    }

    @Test
    public void testRemoveAll() {
        addDataInRepositoriesSize10();
        taskManager.removeAll();
        assertTrue(taskManager.getAllTaskMap().isEmpty());
        assertTrue(taskManager.getAllEpicMap().isEmpty());
        assertTrue(taskManager.getAllSubTaskMap().isEmpty());
        assertTrue(taskManager.getAllMap().isEmpty());
    }

    @Test
    public void testRemoveTaskMap() {
        addDataInRepositoriesSize10();
        taskManager.removeTaskMap();
        assertTrue(taskManager.getAllTaskMap().isEmpty());
    }

    @Test
    public void testRemoveEpicMapAndCheckWhatSubtaskMapIsEmpty() {
        addDataInRepositoriesSize10();
        taskManager.removeEpicMap();
        assertTrue(taskManager.getAllEpicMap().isEmpty());
        assertTrue(taskManager.getAllSubTaskMap().isEmpty());
    }

    @Test
    public void testRemoveSubTaskMapAndCheckWhatEpicChanges() {
        addDataInRepositoriesSize10();
        taskManager.removeSubTaskMap();

        Collection<Epic> values = taskManager.getAllEpicMap().values();

        assertTrue(taskManager.getAllSubTaskMap().isEmpty());
        assertTrue(values.stream().allMatch(epic -> epic.getSubTasks().isEmpty()));
    }

    @Test
    public void checkGetSubtaskByEpicIfExist() {
        addDataInRepositoriesSize10();
        List<SubTask> subTasksByEpic = taskManager.getSubTasksByEpic(taskManager.getEpicById(3));
        assertEquals(2, subTasksByEpic.size());
    }

    @Test
    public void checkGetSubtaskByEpicIfNotExist() {
        assertThrows(NullPointerException.class, () -> taskManager.getSubTasksByEpic(taskManager.getEpicById(3)));
    }

    @Test
    public void checkGetHistoryIfNotExist() {
        assertEquals(0, taskManager.getHistory().size());
    }

    protected void addDataInRepositoriesSize10() {
        taskManager.addTask(new Task(1, TaskType.TASK, "test", TaskStatus.NEW, "test"));
        taskManager.addTask(new Task(2, TaskType.TASK, "test", TaskStatus.NEW, "test"));
        taskManager.addEpic(new Epic(3, TaskType.EPIC, "test", TaskStatus.NEW, "test"));
        taskManager.addEpic(new Epic(4, TaskType.EPIC, "test", TaskStatus.NEW, "test"));
        taskManager.addEpic(new Epic(9, TaskType.EPIC, "test", TaskStatus.NEW, "test"));
        taskManager.addSubTask(9, new SubTask(10, TaskType.SUBTASK, "test", TaskStatus.DONE,
                "test", 9));
        taskManager.addSubTask(3, new SubTask(5, TaskType.SUBTASK, "test", TaskStatus.DONE,
                "test", 3));
        taskManager.addSubTask(3, new SubTask(6, TaskType.SUBTASK, "test", TaskStatus.NEW,
                "test", 3));
        taskManager.addSubTask(4, new SubTask(7, TaskType.SUBTASK, "test", TaskStatus.NEW,
                "test", 4));
        taskManager.addSubTask(4, new SubTask(8, TaskType.SUBTASK, "test", TaskStatus.NEW,
                "test", 4));
    }
}