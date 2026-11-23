
package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.VehicleDAO;
import ua.edu.nupp.kpp.server.dao.RouteDAO;
import ua.edu.nupp.kpp.server.model.Vehicle;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VehicleService {

    private static final Logger LOGGER =
            Logger.getLogger(VehicleService.class.getName());

    private final VehicleDAO vehicleDAO;
    private final RouteDAO routeDAO;

    public VehicleService() {
        this.vehicleDAO = new VehicleDAO();
        this.routeDAO = new RouteDAO();
    }

    // CREATE - створення транспортного засобу
    public void create(Vehicle vehicle) throws SQLException {
        try {
            validate(vehicle);
            validateRoute(vehicle.getRouteId());

            vehicleDAO.create(vehicle);

            LOGGER.info("Створено транспортний засіб ID = "
                    + vehicle.getId());

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка створення транспорту", e);
            throw e;
        }
    }

    // READ - отримання транспорту за ID
    public Vehicle getById(int id) throws SQLException {
        try {
            validateId(id);
            return vehicleDAO.getById(id);

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання транспорту", e);
            throw e;
        }
    }

    // READ - отримання всіх транспортних засобів
    public List<Vehicle> getAll() throws SQLException {
        try {
            return vehicleDAO.getAll();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання списку транспорту", e);
            throw e;
        }
    }

    // UPDATE - редагування транспорту
    public boolean update(Vehicle vehicle) throws SQLException {
        try {
            validate(vehicle);
            validateId(vehicle.getId());
            validateRoute(vehicle.getRouteId());

            boolean updated = vehicleDAO.update(vehicle);

            if (updated) {
                LOGGER.info("Оновлено транспорт ID = "
                        + vehicle.getId());
            }

            return updated;

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка оновлення транспорту", e);
            throw e;
        }
    }

    // DELETE - видалення транспорту
    public boolean delete(int id) throws SQLException {
        try {
            validateId(id);

            boolean deleted = vehicleDAO.delete(id);

            if (deleted) {
                LOGGER.info("Видалено транспорт ID = " + id);
            }

            return deleted;

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка видалення транспорту", e);
            throw e;
        }
    }

    // Валідація транспортного засобу
    private void validate(Vehicle vehicle) {

        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Транспортний засіб не може бути null");
        }

        if (vehicle.getRegistrationNumber() == null ||
                vehicle.getRegistrationNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Реєстраційний номер обов'язковий");
        }

        if (vehicle.getVehicleType() == null ||
                vehicle.getVehicleType().isBlank()) {
            throw new IllegalArgumentException(
                    "Тип транспортного засобу обов'язковий");
        }

        if (vehicle.getModel() == null ||
                vehicle.getModel().isBlank()) {
            throw new IllegalArgumentException(
                    "Модель транспортного засобу обов'язкова");
        }
    }

    // Перевірка існування маршруту в MySQL
    private void validateRoute(Integer routeId)
            throws SQLException {

        // У нашій моделі маршрут може бути не призначений
        if (routeId == null) {
            return;
        }

        validateId(routeId);

        if (routeDAO.getById(routeId) == null) {
            throw new IllegalArgumentException(
                    "Маршрут з ID " + routeId
                            + " не існує в базі даних");
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
