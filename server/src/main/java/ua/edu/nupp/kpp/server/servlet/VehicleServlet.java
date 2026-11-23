
package ua.edu.nupp.kpp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.server.model.Vehicle;
import ua.edu.nupp.kpp.server.service.VehicleService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VehicleServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(VehicleServlet.class.getName());

    private final VehicleService service = new VehicleService();
    private final ObjectMapper mapper = new ObjectMapper();

    // GET - отримання транспортних засобів
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {
            String idParam = request.getParameter("id");

            if (idParam == null || idParam.isBlank()) {

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        service.getAll()
                );

            } else {

                int id = parseId(request);
                Vehicle vehicle = service.getById(id);

                if (vehicle == null) {
                    sendJson(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            Map.of("error",
                                    "Транспортний засіб не знайдено")
                    );
                    return;
                }

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        vehicle
                );
            }

        } catch (IllegalArgumentException e) {

            sendJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("error", errorMessage(e))
            );

        } catch (SQLException e) {

            LOGGER.log(Level.SEVERE,
                    "Помилка отримання транспорту", e);

            sendJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Map.of("error", "Помилка бази даних")
            );
        }
    }

    // POST - створення, оновлення, видалення
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            String action = request.getParameter("action");

            if (action == null || action.isBlank()) {
                throw new IllegalArgumentException(
                        "Не вказано параметр action"
                );
            }

            switch (action) {

                case "create" -> {

                    Vehicle vehicle = readVehicle(request);

                    service.create(vehicle);

                    LOGGER.info(
                            "Створено транспортний засіб ID = "
                                    + vehicle.getId()
                    );

                    sendJson(
                            response,
                            HttpServletResponse.SC_CREATED,
                            vehicle
                    );
                }

                case "update" -> {

                    int id = parseId(request);
                    Vehicle vehicle = readVehicle(request);
                    vehicle.setId(id);

                    boolean updated = service.update(vehicle);

                    sendJson(
                            response,
                            updated
                                    ? HttpServletResponse.SC_OK
                                    : HttpServletResponse.SC_NOT_FOUND,
                            Map.of(
                                    "success", updated,
                                    "message", updated
                                            ? "Транспорт оновлено"
                                            : "Транспорт не знайдено"
                            )
                    );
                }

                case "delete" -> {

                    int id = parseId(request);

                    boolean deleted = service.delete(id);

                    sendJson(
                            response,
                            deleted
                                    ? HttpServletResponse.SC_OK
                                    : HttpServletResponse.SC_NOT_FOUND,
                            Map.of(
                                    "success", deleted,
                                    "message", deleted
                                            ? "Транспорт видалено"
                                            : "Транспорт не знайдено"
                            )
                    );
                }

                default -> throw new IllegalArgumentException(
                        "Невідома дія: " + action
                );
            }

        } catch (IllegalArgumentException e) {

            LOGGER.log(Level.WARNING,
                    "Некоректний запит транспорту", e);

            sendJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("error", errorMessage(e))
            );

        } catch (SQLException e) {

            LOGGER.log(Level.SEVERE,
                    "Помилка операції з транспортом", e);

            sendJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Map.of("error", "Помилка бази даних")
            );
        }
    }

    // Читання параметрів транспортного засобу
    private Vehicle readVehicle(
            HttpServletRequest request
    ) {

        String registrationNumber =
                request.getParameter("registrationNumber");

        String vehicleType =
                request.getParameter("vehicleType");

        String model =
                request.getParameter("model");

        String routeIdParam =
                request.getParameter("routeId");

        if (registrationNumber == null
                || registrationNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Реєстраційний номер обов'язковий"
            );
        }

        if (vehicleType == null || vehicleType.isBlank()) {
            throw new IllegalArgumentException(
                    "Тип транспорту обов'язковий"
            );
        }

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException(
                    "Модель транспорту обов'язкова"
            );
        }

        if (routeIdParam == null || routeIdParam.isBlank()) {
            throw new IllegalArgumentException(
                    "Не вказано routeId"
            );
        }

        int routeId;

        try {
            routeId = Integer.parseInt(routeIdParam);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "routeId повинен бути цілим числом"
            );
        }

        if (routeId <= 0) {
            throw new IllegalArgumentException(
                    "routeId повинен бути більше нуля"
            );
        }

        return new Vehicle(
                0,
                registrationNumber.trim(),
                vehicleType.trim(),
                model.trim(),
                routeId
        );
    }

    // Перевірка ID
    private int parseId(
            HttpServletRequest request
    ) {

        String value = request.getParameter("id");

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Не вказано ID транспорту"
            );
        }

        int id;

        try {
            id = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "ID повинен бути цілим числом"
            );
        }

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID повинен бути більше нуля"
            );
        }

        return id;
    }

    private String errorMessage(Exception e) {
        String message = e.getMessage();

        return message == null || message.isBlank()
                ? "Некоректний запит"
                : message;
    }

    // Відправлення JSON
    private void sendJson(
            HttpServletResponse response,
            int status,
            Object data
    ) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        mapper.writeValue(response.getWriter(), data);
    }
}
