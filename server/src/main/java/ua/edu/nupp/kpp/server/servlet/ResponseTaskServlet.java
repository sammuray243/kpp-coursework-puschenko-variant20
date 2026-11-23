
package ua.edu.nupp.kpp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.server.model.ResponseTask;
import ua.edu.nupp.kpp.server.service.ResponseTaskService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResponseTaskServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(ResponseTaskServlet.class.getName());

    private final ResponseTaskService service =
            new ResponseTaskService();

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules();

    // GET - отримання завдань реагування
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
                int id = positiveInt(request, "id");
                ResponseTask task = service.getById(id);

                if (task == null) {
                    sendJson(response, 404,
                            Map.of("error",
                                    "Завдання реагування не знайдено"));
                    return;
                }

                sendJson(response, 200, task);
            }

        } catch (IllegalArgumentException e) {
            sendJson(response, 400,
                    Map.of("error", message(e)));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання завдань", e);

            sendJson(response, 500,
                    Map.of("error", "Помилка бази даних"));
        }
    }

    // POST - створення, редагування, видалення
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
                    ResponseTask task = readTask(request);
                    service.create(task);

                    LOGGER.info("Створено завдання реагування");

                    sendJson(response, 201, task);
                }

                case "update" -> {
                    int id = positiveInt(request, "id");

                    ResponseTask task = readTask(request);
                    task.setId(id);

                    boolean updated = service.update(task);

                    sendJson(response, updated ? 200 : 404,
                            Map.of(
                                    "success", updated,
                                    "message", updated
                                            ? "Завдання оновлено"
                                            : "Завдання не знайдено"
                            ));
                }

                case "delete" -> {
                    int id = positiveInt(request, "id");

                    boolean deleted = service.delete(id);

                    sendJson(response, deleted ? 200 : 404,
                            Map.of(
                                    "success", deleted,
                                    "message", deleted
                                            ? "Завдання видалено"
                                            : "Завдання не знайдено"
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
                    "Помилка операції із завданням", e);

            sendJson(response, 500,
                    Map.of("error", "Помилка бази даних"));
        }
    }

    // Читання параметрів завдання
    private ResponseTask readTask(
            HttpServletRequest request
    ) {
        int incidentId =
                positiveInt(request, "incidentId");

        String description =
                required(request, "taskDescription");

        String responsiblePerson =
                required(request, "responsiblePerson");

        ResponseTask task = new ResponseTask();

        task.setIncidentId(incidentId);
        task.setTaskDescription(description);
        task.setResponsiblePerson(responsiblePerson);

        return task;
    }

    // Перевірка числових параметрів
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

    // Обов'язкові параметри
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
