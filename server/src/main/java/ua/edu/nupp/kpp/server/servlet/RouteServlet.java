
package ua.edu.nupp.kpp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.edu.nupp.kpp.server.model.Route;
import ua.edu.nupp.kpp.server.service.RouteService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RouteServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(RouteServlet.class.getName());

    private final RouteService service = new RouteService();

    private final ObjectMapper mapper = new ObjectMapper();

    // GET - отримання маршрутів
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        try {
            String idParam = request.getParameter("id");

            if (idParam == null || idParam.isBlank()) {

                // Отримання всіх маршрутів
                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        service.getAll()
                );

            } else {

                // Отримання маршруту за ID
                int id = Integer.parseInt(idParam);

                if (id <= 0) {
                    throw new IllegalArgumentException(
                            "ID повинен бути більше нуля"
                    );
                }

                Route route = service.getById(id);

                if (route == null) {
                    sendJson(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            Map.of(
                                    "error",
                                    "Маршрут не знайдено"
                            )
                    );
                    return;
                }

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        route
                );
            }

        } catch (IllegalArgumentException e) {

            sendJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("error", errorMessage(e))
            );

        } catch (SQLException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Помилка отримання маршрутів",
                    e
            );

            sendJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Map.of(
                            "error",
                            "Помилка бази даних"
                    )
            );
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
                        "Не вказано параметр action"
                );
            }

            switch (action) {

                // CREATE
                case "create" -> {

                    Route route = readRoute(request);

                    service.create(route);

                    LOGGER.info(
                            "Створено маршрут ID = "
                                    + route.getId()
                    );

                    sendJson(
                            response,
                            HttpServletResponse.SC_CREATED,
                            route
                    );
                }

                // UPDATE
                case "update" -> {

                    int id = parseId(request);

                    Route route = readRoute(request);
                    route.setId(id);

                    boolean updated = service.update(route);

                    if (updated) {

                        LOGGER.info(
                                "Оновлено маршрут ID = " + id
                        );

                        sendJson(
                                response,
                                HttpServletResponse.SC_OK,
                                Map.of(
                                        "success", true,
                                        "message",
                                        "Маршрут оновлено"
                                )
                        );

                    } else {

                        sendJson(
                                response,
                                HttpServletResponse.SC_NOT_FOUND,
                                Map.of(
                                        "success", false,
                                        "error",
                                        "Маршрут не знайдено"
                                )
                        );
                    }
                }

                // DELETE
                case "delete" -> {

                    int id = parseId(request);

                    boolean deleted = service.delete(id);

                    if (deleted) {

                        LOGGER.info(
                                "Видалено маршрут ID = " + id
                        );

                        sendJson(
                                response,
                                HttpServletResponse.SC_OK,
                                Map.of(
                                        "success", true,
                                        "message",
                                        "Маршрут видалено"
                                )
                        );

                    } else {

                        sendJson(
                                response,
                                HttpServletResponse.SC_NOT_FOUND,
                                Map.of(
                                        "success", false,
                                        "error",
                                        "Маршрут не знайдено"
                                )
                        );
                    }
                }

                default -> throw new IllegalArgumentException(
                        "Невідома дія: " + action
                );
            }

        } catch (IllegalArgumentException e) {

            LOGGER.log(
                    Level.WARNING,
                    "Некоректний запит маршруту",
                    e
            );

            sendJson(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("error", errorMessage(e))
            );

        } catch (SQLException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Помилка операції з маршрутом",
                    e
            );

            sendJson(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Map.of(
                            "error",
                            "Помилка бази даних"
                    )
            );
        }
    }

    // Читання даних маршруту
    private Route readRoute(
            HttpServletRequest request
    ) {

        String number =
                request.getParameter("routeNumber");

        String name =
                request.getParameter("routeName");

        String importanceParam =
                request.getParameter("importance");

        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException(
                    "Номер маршруту обов'язковий"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Назва маршруту обов'язкова"
            );
        }

        if (importanceParam == null
                || importanceParam.isBlank()) {
            throw new IllegalArgumentException(
                    "Не вказано importance"
            );
        }

        int importance;

        try {
            importance = Integer.parseInt(
                    importanceParam
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "importance повинен бути цілим числом"
            );
        }

        if (importance < 0 || importance > 100) {
            throw new IllegalArgumentException(
                    "Важливість повинна бути від 0 до 100"
            );
        }

        return new Route(
                0,
                number.trim(),
                name.trim(),
                importance
        );
    }

    // Перевірка ID
    private int parseId(
            HttpServletRequest request
    ) {

        String value = request.getParameter("id");

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Не вказано ID маршруту"
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

    // Безпечне повідомлення про помилку
    private String errorMessage(Exception e) {

        String message = e.getMessage();

        return message == null || message.isBlank()
                ? "Некоректний запит"
                : message;
    }

    // Надсилання JSON
    private void sendJson(
            HttpServletResponse response,
            int status,
            Object data
    ) throws IOException {

        response.setStatus(status);

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding("UTF-8");

        mapper.writeValue(
                response.getWriter(),
                data
        );
    }
}
