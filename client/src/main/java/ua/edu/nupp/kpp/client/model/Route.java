
package ua.edu.nupp.kpp.client.model;

public class Route {

    private int id;
    private String routeNumber;
    private String routeName;
    private int importance;

    public Route() {
    }

    public Route(int id, String routeNumber,
                 String routeName, int importance) {
        this.id = id;
        this.routeNumber = routeNumber;
        this.routeName = routeName;
        this.importance = importance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    @Override
    public String toString() {
        return routeNumber + " - " + routeName;
    }
}
