package services.status;

import models.task.Epic;
import models.task.SubTask;
import models.task.TaskStatus;

import java.util.HashMap;
import java.util.List;

public class StatusManagerImpl implements StatusManager {
    @Override
    public TaskStatus getEpicStatus(List<SubTask> subTasks) {

        boolean isNew = subTasks.stream().allMatch(subTask -> subTask.getStatus().equals(TaskStatus.NEW));
        boolean isDone = subTasks.stream().allMatch(subTask -> subTask.getStatus().equals(TaskStatus.DONE));

        if (subTasks.isEmpty() || isNew) {
            return TaskStatus.NEW;
        } else if (isDone) {
            return TaskStatus.DONE;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }

    @Override
    public TaskStatus setEpicStatus(Epic epic, HashMap<Integer,Epic> epicRepository) {
        Epic task = epicRepository.get(epic.getId());
        List<SubTask> subTasks = task.getSubTasks();
        return getEpicStatus(subTasks);
    }
}
