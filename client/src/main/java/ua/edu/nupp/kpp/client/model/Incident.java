
package ua.edu.nupp.kpp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Incident {

    private int id;
    private int vehicleId;
    private int routeId;
    private String description;
    private LocalDateTime incidentDate;
    private String status;

    private double f1;
    private double f2;
    private double f3;
    private double integralScore;

    public Incident() {
    }

    public Incident(int id,
                    int vehicleId,
                    int routeId,
                    String description,
                    LocalDateTime incidentDate,
                    String status,
                    double f1,
                    double f2,
                    double f3) {

        this.id = id;
        this.vehicleId = vehicleId;
        this.routeId = routeId;
        this.description = description;
        this.incidentDate = incidentDate;
        this.status = status;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;

        this.integralScore = calculateIndicator();
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

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public double getIntegralScore() {
        return integralScore;
    }

    public void setIntegralScore(double integralScore) {
        this.integralScore = integralScore;
    }

    public double calculateIndicator() {
        return 0.40 * f1
                + 0.35 * f2
                + 0.25 * f3;
    }

    @Override
    public String toString() {
        return "Інцидент №" + id
                + " - " + description
                + " (" + status + ")";
    }
}
