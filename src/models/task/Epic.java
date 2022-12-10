package models.task;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task{

    private List<SubTask> subTasks;

    public Epic() {
    }

    public Epic(Integer id, String title, String description) {
        super(id, title, description , TaskStatus.NEW);
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
