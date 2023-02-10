package services.util;

import models.task.SubTask;
import models.task.Task;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public final class TimeManagerUtil {

    private TimeManagerUtil() {
    }

    public static LocalDateTime getStartTimeForEpic(List<SubTask> subTasks) {
        Optional<LocalDateTime> start = subTasks.stream()
                .map(SubTask::getStartTime)
                .min(LocalDateTime::compareTo);

        if (start.isPresent()) {
            return start.get();
        } else {
            throw new IllegalArgumentException("Time Error");
        }
    }

    public static LocalDateTime getEndTimeForEpic(List<SubTask> subTasks) {
        Optional<LocalDateTime> end = subTasks.stream()
                .map(SubTask::getEndTime)
                .max(LocalDateTime::compareTo);

        if (end.isPresent()) {
            return end.get();
        } else {
            throw new IllegalArgumentException("Time Error");
        }
    }

    public static boolean checkSameStartAndProgressTimeTasks(Task task, HashMap<Integer, Task> tasksMap) {
        if (task.getStartTime() == null) return false;
        for (Task t : tasksMap.values()) {
            if (t.getStartTime() == null) continue;
            boolean b = (task.getStartTime().isEqual(t.getStartTime()) || task.getEndTime().isEqual(t.getEndTime()))
                    || (task.getStartTime().isBefore(t.getEndTime()) && task.getStartTime().isAfter(t.getStartTime()));
            if (b) {
                return false;
            }
        }
        return true;
    }
}
