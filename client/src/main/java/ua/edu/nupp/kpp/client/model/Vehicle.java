package ua.edu.nupp.kpp.client.model;

public class Vehicle {

    private int id;
    private String registrationNumber;
    private String vehicleType;
    private String model;
    private int routeId;

    public Vehicle() {
    }

    public Vehicle(int id,
                   String registrationNumber,
                   String vehicleType,
                   String model,
                   int routeId) {

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

    public void setRegistrationNumber(
            String registrationNumber) {
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

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    @Override
    public String toString() {
        return registrationNumber
                + " - "
                + vehicleType
                + " ("
                + model
                + ")";
    }
}
