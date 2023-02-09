package test.models.task;

import models.task.Epic;
import models.task.SubTask;
import models.task.TaskStatus;
import models.task.TaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import services.status.StatusManager;
import services.status.StatusManagerImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Model Epic task tests.")
class EpicTest {

    private final StatusManager statusManager = new StatusManagerImpl();

    @Test
    public void whenEpicHaveEmptySubtaskList(){
        Epic epic = new Epic();
        assertNull(epic.getSubTasks());
    }

    @Test
    public void whenInEpicAllSubtasksHaveStatusNew(){
        Epic epic = new Epic(1, TaskType.EPIC,"test", TaskStatus.NEW,"test");
        epic.setSubTasks(getSubTasksList(TaskStatus.NEW));
        epic.setStatus(statusManager.getEpicStatus(epic.getSubTasks()));

        assertEquals(epic.getStatus(), TaskStatus.NEW);
    }

    @Test
    public void whenInEpicAllSubtasksHaveStatusDone(){
        Epic epic = new Epic(1, TaskType.EPIC,"test", TaskStatus.NEW,"test");
        epic.setSubTasks(getSubTasksList(TaskStatus.DONE));
        epic.setStatus(statusManager.getEpicStatus(epic.getSubTasks()));

        assertEquals(epic.getStatus(), TaskStatus.DONE);
    }

    @Test
    public void whenInEpicAllSubtasksHaveStatusInProgress(){
        Epic epic = new Epic(1, TaskType.EPIC,"test", TaskStatus.NEW,"test");
        epic.setSubTasks(getSubTasksList(TaskStatus.IN_PROGRESS));
        epic.setStatus(statusManager.getEpicStatus(epic.getSubTasks()));

        assertEquals(epic.getStatus(), TaskStatus.IN_PROGRESS);
    }

    @Test
    public void whenEpicHaveStatusInProgressIfHaveOneStatusDone(){
        Epic epic = new Epic(1, TaskType.EPIC,"test", TaskStatus.NEW,"test");

        List<SubTask> subTasksList = new ArrayList<>();
        subTasksList.add(new SubTask(4, TaskType.SUBTASK, "test", TaskStatus.DONE, "test", 1));
        subTasksList.addAll(getSubTasksList(TaskStatus.NEW));

        epic.setSubTasks(subTasksList);
        epic.setStatus(statusManager.getEpicStatus(epic.getSubTasks()));

        assertEquals(epic.getStatus(), TaskStatus.IN_PROGRESS);
    }
    public static List<SubTask> getSubTasksList(TaskStatus status){
        return List.of(new SubTask(2, TaskType.SUBTASK, "test", status, "test", 1)
                , new SubTask(3, TaskType.SUBTASK, "test", status, "test", 1));
    }
}