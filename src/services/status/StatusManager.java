package services.status;

import models.task.Epic;
import models.task.TaskStatus;

public interface StatusManager {
    TaskStatus setEpicStatus(Epic epic);
}
