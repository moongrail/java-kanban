package models.task;

public class SubTask extends Task {
    private Integer idEpic;

    public SubTask() {
    }

    public SubTask(Integer id, TaskType type, String title, TaskStatus status, String description, Integer idEpic) {
        super(id, type, title, status, description);
        this.idEpic = idEpic;
    }

    public Integer getIdEpic() {
        return idEpic;
    }

    public void setIdEpic(Integer idEpic) {
        this.idEpic = idEpic;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "idEpic=" + idEpic +
                ", id=" + super.getId() +
                ", title='" + super.getTitle() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                ", status=" + super.getStatus() +
                '}';
    }
}