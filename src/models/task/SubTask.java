package models.task;

public class SubTask extends Task{

    private Integer idEpic;




    public SubTask() {
    }

    public SubTask(Integer id, String title, String description, TaskStatus status, Integer idEpic) {
        super(id, title, description, status);
        this.idEpic = idEpic;
    }

    public Integer getIdEpic() {
        return idEpic;
    }

    public void setIdEpic(Integer idEpic) {
        this.idEpic = idEpic;
    }

}
