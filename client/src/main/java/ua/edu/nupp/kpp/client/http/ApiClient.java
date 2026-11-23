
package ua.edu.nupp.kpp.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ua.edu.nupp.kpp.client.model.Route;
import ua.edu.nupp.kpp.client.model.Vehicle;
import ua.edu.nupp.kpp.client.model.Incident;
import ua.edu.nupp.kpp.client.model.ResponseTask;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiClient {

    private static final String BASE_URL =
            "http://localhost:8080/server/api";

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ApiClient() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    // HTTP GET
    private String get(String endpoint)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        return execute(request);
    }

    // HTTP POST
    private String post(
            String endpoint,
            Map<String, String> parameters
    ) throws IOException, InterruptedException {

        String formData = parameters.entrySet()
                .stream()
                .map(entry ->
                        encode(entry.getKey()) + "="
                                + encode(entry.getValue()))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(Duration.ofSeconds(15))
                .header(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                )
                .POST(HttpRequest.BodyPublishers.ofString(
                        formData, StandardCharsets.UTF_8))
                .build();

        return execute(request);
    }

    private String execute(HttpRequest request)
            throws IOException, InterruptedException {

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8)
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "HTTP " + response.statusCode()
                            + ": " + response.body()
            );
        }

        return response.body();
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value, StandardCharsets.UTF_8);
    }

    // ==========================
    // МАРШРУТИ
    // ==========================

    public List<Route> getRoutes()
            throws IOException, InterruptedException {

        String json = get("/routes");

        return mapper.readValue(
                json,
                mapper.getTypeFactory()
                        .constructCollectionType(
                                List.class, Route.class)
        );
    }

    public String createRoute(Route route)
            throws IOException, InterruptedException {

        return post("/routes", Map.of(
                "action", "create",
                "routeNumber", route.getRouteNumber(),
                "routeName", route.getRouteName(),
                "importance",
                String.valueOf(route.getImportance())
        ));
    }

    public String updateRoute(Route route)
            throws IOException, InterruptedException {

        return post("/routes", Map.of(
                "action", "update",
                "id", String.valueOf(route.getId()),
                "routeNumber", route.getRouteNumber(),
                "routeName", route.getRouteName(),
                "importance",
                String.valueOf(route.getImportance())
        ));
    }

    public String deleteRoute(int id)
            throws IOException, InterruptedException {

        return post("/routes", Map.of(
                "action", "delete",
                "id", String.valueOf(id)
        ));
    }

    // ==========================
    // ТРАНСПОРТНІ ЗАСОБИ
    // ==========================

    public List<Vehicle> getVehicles()
            throws IOException, InterruptedException {

        String json = get("/vehicles");

        return mapper.readValue(
                json,
                mapper.getTypeFactory()
                        .constructCollectionType(
                                List.class, Vehicle.class)
        );
    }

    public String createVehicle(Vehicle vehicle)
            throws IOException, InterruptedException {

        return post("/vehicles", Map.of(
                "action", "create",
                "registrationNumber",
                vehicle.getRegistrationNumber(),
                "vehicleType", vehicle.getVehicleType(),
                "model", vehicle.getModel(),
                "routeId",
                String.valueOf(vehicle.getRouteId())
        ));
    }

    public String updateVehicle(Vehicle vehicle)
            throws IOException, InterruptedException {

        return post("/vehicles", Map.of(
                "action", "update",
                "id", String.valueOf(vehicle.getId()),
                "registrationNumber",
                vehicle.getRegistrationNumber(),
                "vehicleType", vehicle.getVehicleType(),
                "model", vehicle.getModel(),
                "routeId",
                String.valueOf(vehicle.getRouteId())
        ));
    }

    public String deleteVehicle(int id)
            throws IOException, InterruptedException {

        return post("/vehicles", Map.of(
                "action", "delete",
                "id", String.valueOf(id)
        ));
    }

    // ==========================
    // ІНЦИДЕНТИ
    // ==========================

    public List<Incident> getIncidents()
            throws IOException, InterruptedException {

        String json = get("/incidents");

        return mapper.readValue(
                json,
                mapper.getTypeFactory()
                        .constructCollectionType(
                                List.class, Incident.class)
        );
    }

    public String createIncident(Incident incident)
            throws IOException, InterruptedException {

        return post("/incidents", Map.of(
                "action", "create",
                "vehicleId",
                String.valueOf(incident.getVehicleId()),
                "routeId",
                String.valueOf(incident.getRouteId()),
                "description", incident.getDescription()
        ));
    }

    public String deleteIncident(int id)
            throws IOException, InterruptedException {

        return post("/incidents", Map.of(
                "action", "delete",
                "id", String.valueOf(id)
        ));
    }

    public String changeIncidentStatus(
            int id, String status
    ) throws IOException, InterruptedException {

        return post("/incidents", Map.of(
                "action", "changeStatus",
                "id", String.valueOf(id),
                "status", status
        ));
    }

    // ==========================
    // ЗАВДАННЯ РЕАГУВАННЯ
    // ==========================

    public List<ResponseTask> getTasks()
            throws IOException, InterruptedException {

        String json = get("/tasks");

        return mapper.readValue(
                json,
                mapper.getTypeFactory()
                        .constructCollectionType(
                                List.class, ResponseTask.class)
        );
    }

    public String createTask(ResponseTask task)
            throws IOException, InterruptedException {

        return post("/tasks", Map.of(
                "action", "create",
                "incidentId",
                String.valueOf(task.getIncidentId()),
                "taskDescription",
                task.getTaskDescription(),
                "responsiblePerson",
                task.getResponsiblePerson()
        ));
    }

    public String updateTask(ResponseTask task)
            throws IOException, InterruptedException {

        return post("/tasks", Map.of(
                "action", "update",
                "id", String.valueOf(task.getId()),
                "incidentId",
                String.valueOf(task.getIncidentId()),
                "taskDescription",
                task.getTaskDescription(),
                "responsiblePerson",
                task.getResponsiblePerson()
        ));
    }

    public String deleteTask(int id)
            throws IOException, InterruptedException {

        return post("/tasks", Map.of(
                "action", "delete",
                "id", String.valueOf(id)
        ));
    }
}
