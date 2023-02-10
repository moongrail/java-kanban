package test.services.util;

import models.task.*;
import org.junit.jupiter.api.Test;
import services.util.TimeManagerUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeManagerUtilTest {

    @Test
    public void whenGetStartTimeForEpicCorrect() {
        List<SubTask> subTasks = getDataSubTaskList();
        assertEquals(LocalDateTime.of(2021, 1, 1, 0, 0)
                , TimeManagerUtil.getStartTimeForEpic(subTasks));
    }

    @Test
    public void whenGetEndTimeForEpicCorrect() {
        List<SubTask> subTasks = getDataSubTaskList();
        assertEquals(LocalDateTime.of(2021, 1, 4, 0, 0)
                , TimeManagerUtil.getEndTimeForEpic(subTasks));
    }

    @Test
    public void whenGetEndTimeForEpicInCorrect() {
        assertThrows(NullPointerException.class, () -> TimeManagerUtil.getEndTimeForEpic(null));
        List<SubTask> subTasks = Collections.singletonList(new SubTask(10, TaskType.SUBTASK, "test",
                TaskStatus.DONE,
                "test", 9));
        assertThrows(NullPointerException.class, () -> TimeManagerUtil.getEndTimeForEpic(subTasks));
    }

    @Test
    public void whenGetStartTimeForEpicInCorrect() {
        assertThrows(NullPointerException.class, () -> TimeManagerUtil.getStartTimeForEpic(null));
        List<SubTask> subTasks = Collections.singletonList(new SubTask(10, TaskType.SUBTASK, "test",
                TaskStatus.DONE,
                "test", 9));
        assertThrows(NullPointerException.class, () -> TimeManagerUtil.getStartTimeForEpic(subTasks));
    }

    @Test
    public void checkSameStartAndProgressTimeTasksHaveSame() {
        HashMap<Integer, Task> tasksMap = new HashMap<>();

        SubTask subTaskOne = new SubTask(5, TaskType.SUBTASK, "test", TaskStatus.NEW,
                "test",
                Duration.of(1, ChronoUnit.DAYS),
                LocalDateTime.of(2021, 1, 1, 0, 0),
                3);
        SubTask subTaskTwo = new SubTask(5, TaskType.SUBTASK, "test", TaskStatus.NEW,
                "test",
                Duration.of(1, ChronoUnit.DAYS),
                LocalDateTime.of(2021, 1, 1, 0, 0),
                3);

        tasksMap.put(1, subTaskOne);

        assertFalse(TimeManagerUtil.checkSameStartAndProgressTimeTasks(subTaskTwo, tasksMap));
    }

    @Test
    public void checkSameStartAndProgressTimeTasksNoSame() {
        HashMap<Integer, Task> tasksMap = new HashMap<>();

        SubTask subTaskOne = new SubTask(5, TaskType.SUBTASK, "test", TaskStatus.NEW,
                "test",
                Duration.of(1, ChronoUnit.DAYS),
                LocalDateTime.of(2031, 1, 1, 0, 0),
                3);
        SubTask subTaskTwo = new SubTask(5, TaskType.SUBTASK, "test", TaskStatus.NEW,
                "test",
                Duration.of(1, ChronoUnit.DAYS),
                LocalDateTime.of(2021, 1, 1, 0, 0),
                3);

        tasksMap.put(1, subTaskOne);

        assertTrue(TimeManagerUtil.checkSameStartAndProgressTimeTasks(subTaskTwo, tasksMap));
    }

    private List<SubTask> getDataSubTaskList() {
        List<SubTask> subTasks = List.of(new SubTask(5, TaskType.SUBTASK, "test", TaskStatus.NEW,
                        "test",
                        Duration.of(1, ChronoUnit.DAYS),
                        LocalDateTime.of(2021, 1, 1, 0, 0),
                        3),
                new SubTask(6, TaskType.SUBTASK, "test", TaskStatus.NEW,
                        "test",
                        Duration.of(2, ChronoUnit.DAYS),
                        LocalDateTime.of(2021, 1, 2, 0, 0), 3));
        return subTasks;
    }
}