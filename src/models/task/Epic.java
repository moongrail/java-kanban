package models.task;

import java.util.List;
import java.util.Objects;

public class Epic extends Task{

    private List<SubTask> subTasks;

    public Epic() {
    }

    public Epic(Integer id, String title, String description) {
        super(id, title, description);
    }

    public List<SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<SubTask> subTasks) {
        this.subTasks = subTasks;
    }
}
