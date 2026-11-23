package ua.edu.nupp.kpp.server.model;

public class Vehicle {

    private int id;
    private String registrationNumber;
    private String vehicleType;
    private String model;
    private Integer routeId;

    public Vehicle() {
    }

    public Vehicle(int id, String registrationNumber,
                   String vehicleType, String model,
                   Integer routeId) {
        this.id = id;
        this.registrationNumber = registrationNumber;
        this.vehicleType = vehicleType;
        this.model = model;
        this.routeId = routeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", model='" + model + '\'' +
                ", routeId=" + routeId +
                '}';
    }
}
