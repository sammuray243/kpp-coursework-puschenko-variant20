
package ua.edu.nupp.kpp.server.model;

public class ResponseTask {

    private int id;
    private int incidentId;
    private String taskDescription;
    private String responsiblePerson;
    private String taskStatus;

    public ResponseTask() {
    }

    public ResponseTask(int id, int incidentId,
                        String taskDescription,
                        String responsiblePerson,
                        String taskStatus) {
        this.id = id;
        this.incidentId = incidentId;
        this.taskDescription = taskDescription;
        this.responsiblePerson = responsiblePerson;
        this.taskStatus = taskStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(int incidentId) {
        this.incidentId = incidentId;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(String responsiblePerson) {
        this.responsiblePerson = responsiblePerson;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public String toString() {
        return "ResponseTask{" +
                "id=" + id +
                ", incidentId=" + incidentId +
                ", taskDescription='" + taskDescription + '\'' +
                ", responsiblePerson='" + responsiblePerson + '\'' +
                ", taskStatus='" + taskStatus + '\'' +
                '}';
    }
}
