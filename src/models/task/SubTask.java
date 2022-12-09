package models.task;

public class SubTask extends Task{

    private Integer idEpic;
    private String nameEpic;

    public Integer getIdEpic() {
        return idEpic;
    }

    public void setIdEpic(Integer idEpic) {
        this.idEpic = idEpic;
    }

    public String getNameEpic() {
        return nameEpic;
    }

    public void setNameEpic(String nameEpic) {
        this.nameEpic = nameEpic;
    }
}
