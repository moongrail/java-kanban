package services.manager.history;

import models.task.Task;
import models.task.TaskStatus;
import models.task.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void whenHistoryIsBlank() {
        assertEquals(new ArrayList<>(), historyManager.getHistory());
    }

    @Test
    public void historyDontHaveDuplicates() {
        historyManager.add(new Task(1, TaskType.TASK, "test", TaskStatus.NEW, "test"));
        historyManager.add(new Task(1, TaskType.TASK, "test", TaskStatus.NEW, "test"));

        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    public void checkHistoryReverseIteration() {
        historyManager.add(new Task(2, TaskType.TASK, "test", TaskStatus.NEW, "test"));
        historyManager.add(new Task(3, TaskType.TASK, "test", TaskStatus.NEW, "test"));

        assertEquals(2, historyManager.getHistory().size());
        assertArrayEquals(new int[]{3, 2}, historyManager.getHistory().stream()
                .mapToInt(Task::getId)
                .toArray());
    }

    @Test
    public void removeFirstFromHistory() {
        uploadDataInHistory();

        historyManager.remove(1);
        assertFalse(historyManager.getHistory().stream()
                .anyMatch(task -> task.getId().equals(1)));
    }


    @Test
    public void removeLastFromHistory() {
        uploadDataInHistory();

        historyManager.remove(3);
        assertFalse(historyManager.getHistory().stream()
                .anyMatch(task -> task.getId().equals(3)));
    }

    @Test
    public void removeMiddleFromHistory() {
        uploadDataInHistory();

        historyManager.remove(2);
        assertFalse(historyManager.getHistory().stream()
                .anyMatch(task -> task.getId().equals(2)));
    }

    private static List<Task> generateDataHistoryList() {
        return List.of(new Task(1, TaskType.TASK, "test", TaskStatus.NEW, "test")
                , new Task(2, TaskType.TASK, "test", TaskStatus.NEW, "test")
                , new Task(3, TaskType.TASK, "test", TaskStatus.NEW, "test"));
    }

    private void uploadDataInHistory() {
        List<Task> tasks = generateDataHistoryList();
        for (Task task : tasks) {
            historyManager.add(task);
        }
    }
}