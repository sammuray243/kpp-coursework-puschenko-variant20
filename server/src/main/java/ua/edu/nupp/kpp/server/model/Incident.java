
package ua.edu.nupp.kpp.server.model;

import java.time.LocalDateTime;

public class Incident {

    private int id;
    private int vehicleId;
    private int routeId;
    private String description;
    private LocalDateTime incidentDate;
    private String status;
    private int f1;
    private int f2;
    private int f3;
    private double integralScore;

    public Incident() {
    }

    public Incident(int id, int vehicleId, int routeId,
                    String description, LocalDateTime incidentDate,
                    String status, int f1, int f2, int f3,
                    double integralScore) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.routeId = routeId;
        this.description = description;
        this.incidentDate = incidentDate;
        this.status = status;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.integralScore = integralScore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDateTime incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getF1() {
        return f1;
    }

    public void setF1(int f1) {
        this.f1 = f1;
    }

    public int getF2() {
        return f2;
    }

    public void setF2(int f2) {
        this.f2 = f2;
    }

    public int getF3() {
        return f3;
    }

    public void setF3(int f3) {
        this.f3 = f3;
    }

    public double getIntegralScore() {
        return integralScore;
    }

    public void setIntegralScore(double integralScore) {
        this.integralScore = integralScore;
    }

    @Override
    public String toString() {
        return "Incident{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", routeId=" + routeId +
                ", description='" + description + '\'' +
                ", incidentDate=" + incidentDate +
                ", status='" + status + '\'' +
                ", f1=" + f1 +
                ", f2=" + f2 +
                ", f3=" + f3 +
                ", integralScore=" + integralScore +
                '}';
    }
}
