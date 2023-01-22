package models.task;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<SubTask> subTasks;

    public Epic() {
    }

    public Epic(Integer id, TaskType type, String title, TaskStatus status, String description) {
        super(id, type, title, status, description);
        subTasks = new ArrayList<>();
    }

    public List<SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<SubTask> subTasks) {
        this.subTasks = subTasks;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + super.getId() +
                ", title='" + super.getTitle() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                ", status=" + super.getStatus() +
                "\nsubTasks=" + subTasks +
                '}';
    }
}