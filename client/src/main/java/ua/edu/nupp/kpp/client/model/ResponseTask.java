
package ua.edu.nupp.kpp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseTask {

    private int id;
    private int incidentId;
    private String taskDescription;
    private String responsiblePerson;

    public ResponseTask() {
    }

    public ResponseTask(int id,
                        int incidentId,
                        String taskDescription,
                        String responsiblePerson) {

        this.id = id;
        this.incidentId = incidentId;
        this.taskDescription = taskDescription;
        this.responsiblePerson = responsiblePerson;
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

    @Override
    public String toString() {
        return "Завдання №" + id
                + " | Інцидент №" + incidentId
                + " | " + taskDescription
                + " | Відповідальний: "
                + responsiblePerson;
    }
}
