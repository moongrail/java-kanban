package services.status;

import models.task.Epic;
import models.task.SubTask;
import models.task.TaskStatus;

import java.util.HashMap;
import java.util.List;

public interface StatusManager {
    TaskStatus getEpicStatus(List<SubTask> subTasks);
    TaskStatus setEpicStatus(Epic epic, HashMap<Integer,Epic> epicRepository);
}
