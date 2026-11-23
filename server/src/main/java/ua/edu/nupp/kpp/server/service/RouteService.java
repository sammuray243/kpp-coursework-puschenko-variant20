
package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.RouteDAO;
import ua.edu.nupp.kpp.server.model.Route;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RouteService {

    private static final Logger LOGGER =
            Logger.getLogger(RouteService.class.getName());

    private final RouteDAO routeDAO;

    public RouteService() {
        this.routeDAO = new RouteDAO();
    }

    // CREATE - створення маршруту
    public void create(Route route) throws SQLException {
        try {
            validate(route);
            routeDAO.create(route);

            LOGGER.info("Створено маршрут ID = "
                    + route.getId());

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка створення маршруту", e);
            throw e;
        }
    }

    // READ - отримання маршруту за ID
    public Route getById(int id) throws SQLException {
        validateId(id);

        try {
            return routeDAO.getById(id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання маршруту", e);
            throw e;
        }
    }

    // READ - отримання всіх маршрутів
    public List<Route> getAll() throws SQLException {
        try {
            return routeDAO.getAll();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання списку маршрутів", e);
            throw e;
        }
    }

    // UPDATE - редагування маршруту
    public boolean update(Route route) throws SQLException {
        try {
            validate(route);
            validateId(route.getId());

            boolean updated = routeDAO.update(route);

            if (updated) {
                LOGGER.info("Оновлено маршрут ID = "
                        + route.getId());
            }

            return updated;

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка оновлення маршруту", e);
            throw e;
        }
    }

    // DELETE - видалення маршруту
    public boolean delete(int id) throws SQLException {
        try {
            validateId(id);

            boolean deleted = routeDAO.delete(id);

            if (deleted) {
                LOGGER.info("Видалено маршрут ID = " + id);
            }

            return deleted;

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка видалення маршруту", e);
            throw e;
        }
    }

    // Перевірка коректності даних маршруту
    private void validate(Route route) {

        if (route == null) {
            throw new IllegalArgumentException(
                    "Маршрут не може бути null");
        }

        if (route.getRouteNumber() == null ||
                route.getRouteNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Номер маршруту обов'язковий");
        }

        if (route.getRouteName() == null ||
                route.getRouteName().isBlank()) {
            throw new IllegalArgumentException(
                    "Назва маршруту обов'язкова");
        }

        if (route.getImportance() < 0 ||
                route.getImportance() > 100) {
            throw new IllegalArgumentException(
                    "Важливість маршруту має бути від 0 до 100");
        }
    }

    // Перевірка ID
    private void validateId(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID повинен бути додатним числом");
        }
    }
}
