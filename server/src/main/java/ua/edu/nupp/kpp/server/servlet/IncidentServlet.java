
package ua.edu.nupp.kpp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.server.model.Incident;
import ua.edu.nupp.kpp.server.service.IncidentService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IncidentServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(IncidentServlet.class.getName());

    private final IncidentService service = new IncidentService();
    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {
            String idParam = request.getParameter("id");

            if (idParam == null || idParam.isBlank()) {
                sendJson(response, 200, service.getAll());
            } else {
                int id = parseId(request);
                Incident incident = service.getById(id);

                if (incident == null) {
                    sendJson(response, 404,
                            Map.of("error", "Інцидент не знайдено"));
                    return;
                }

                sendJson(response, 200, incident);
            }

        } catch (IllegalArgumentException e) {
            sendJson(response, 400,
                    Map.of("error", message(e)));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання інцидентів", e);
            sendJson(response, 500,
                    Map.of("error", "Помилка бази даних"));
        }
    }

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
                        "Не вказано параметр action");
            }

            switch (action) {

                case "create" -> {
                    Incident incident = readIncident(request);
                    service.create(incident);
                    sendJson(response, 201, incident);
                }

                case "update" -> {
                    Incident incident = readIncident(request);
                    incident.setId(parseId(request));

                    boolean updated = service.update(incident);

                    sendJson(response, updated ? 200 : 404,
                            Map.of("success", updated));
                }

                case "delete" -> {
                    boolean deleted =
                            service.delete(parseId(request));

                    sendJson(response, deleted ? 200 : 404,
                            Map.of("success", deleted));
                }

                case "changeStatus" -> {
                    int id = parseId(request);
                    String status = required(
                            request, "status");

                    service.changeStatus(id, status);

                    sendJson(response, 200,
                            Map.of(
                                    "success", true,
                                    "message", "Статус змінено"
                            ));
                }

                default -> throw new IllegalArgumentException(
                        "Невідома дія: " + action);
            }

        } catch (IllegalArgumentException e) {
            sendJson(response, 400,
                    Map.of("error", message(e)));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка операції з інцидентом", e);
            sendJson(response, 500,
                    Map.of("error", "Помилка бази даних"));
        }
    }

    private Incident readIncident(
            HttpServletRequest request
    ) {
        Incident incident = new Incident();

        incident.setVehicleId(
                positiveInt(request, "vehicleId"));

        incident.setRouteId(
                positiveInt(request, "routeId"));

        incident.setDescription(
                required(request, "description"));

        // Статус не встановлюється з HTTP-запиту.
        // Початковий статус задає IncidentService.
        return incident;
    }

    private int parseId(HttpServletRequest request) {
        return positiveInt(request, "id");
    }

    private int positiveInt(
            HttpServletRequest request,
            String parameter
    ) {
        String value = required(request, parameter);

        try {
            int number = Integer.parseInt(value);

            if (number <= 0) {
                throw new IllegalArgumentException(
                        parameter + " повинен бути більше нуля");
            }

            return number;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    parameter + " повинен бути цілим числом");
        }
    }

    private String required(
            HttpServletRequest request,
            String parameter
    ) {
        String value = request.getParameter(parameter);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Не вказано параметр " + parameter);
        }

        return value.trim();
    }

    private String message(Exception e) {
        return e.getMessage() == null
                ? "Некоректний запит"
                : e.getMessage();
    }

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
