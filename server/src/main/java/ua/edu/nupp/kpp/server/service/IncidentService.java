
package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.IncidentDAO;
import ua.edu.nupp.kpp.server.dao.RouteDAO;
import ua.edu.nupp.kpp.server.dao.VehicleDAO;

import ua.edu.nupp.kpp.server.model.Incident;
import ua.edu.nupp.kpp.server.model.Route;
import ua.edu.nupp.kpp.server.model.Vehicle;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IncidentService {

    private static final Logger LOGGER =
            Logger.getLogger(IncidentService.class.getName());

    private static final String REPORTED = "ПОВІДОМЛЕНО";
    private static final String CONFIRMED = "ПІДТВЕРДЖЕНО";
    private static final String RESPONDING = "РЕАГУВАННЯ";
    private static final String CLOSED = "ЗАКРИТО";

    private final IncidentDAO incidentDAO;
    private final RouteDAO routeDAO;
    private final VehicleDAO vehicleDAO;

    public IncidentService() {
        incidentDAO = new IncidentDAO();
        routeDAO = new RouteDAO();
        vehicleDAO = new VehicleDAO();
    }

    // CREATE - створення нового інциденту
    public void create(Incident incident) throws SQLException {
        try {
            validate(incident);
            validateRelations(incident);

            // Новий інцидент завжди починається
            // зі стану ПОВІДОМЛЕНО
            incident.setStatus(REPORTED);

            if (incident.getIncidentDate() == null) {
                incident.setIncidentDate(LocalDateTime.now());
            }

            incident.setIntegralScore(
                    calculateIntegralScore(
                            incident.getF1(),
                            incident.getF2(),
                            incident.getF3()
                    )
            );

            incidentDAO.create(incident);

            LOGGER.info("Створено інцидент ID = "
                    + incident.getId());

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка створення інциденту", e);
            throw e;
        }
    }

    // READ - отримання інциденту за ID
    public Incident getById(int id) throws SQLException {
        try {
            validateId(id);
            return incidentDAO.getById(id);

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання інциденту", e);
            throw e;
        }
    }

    // READ - отримання всіх інцидентів
    public List<Incident> getAll() throws SQLException {
        try {
            return incidentDAO.getAll();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання списку інцидентів", e);
            throw e;
        }
    }

    // UPDATE - редагування інциденту
    public boolean update(Incident incident) throws SQLException {
        try {
            validate(incident);
            validateId(incident.getId());
            validateRelations(incident);

            Incident existing =
                    incidentDAO.getById(incident.getId());

            if (existing == null) {
                throw new IllegalArgumentException(
                        "Інцидент не знайдено");
            }

            // Забороняємо зміну статусу через update()
            // Для цього існує changeStatus()
            if (!existing.getStatus().equals(incident.getStatus())) {
                throw new IllegalArgumentException(
                        "Статус можна змінювати лише через changeStatus()");
            }

            incident.setIntegralScore(
                    calculateIntegralScore(
                            incident.getF1(),
                            incident.getF2(),
                            incident.getF3()
                    )
            );

            boolean updated = incidentDAO.update(incident);

            if (updated) {
                LOGGER.info("Оновлено інцидент ID = "
                        + incident.getId());
            }

            return updated;

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка оновлення інциденту", e);
            throw e;
        }
    }

    // DELETE - видалення інциденту
    public boolean delete(int id) throws SQLException {
        try {
            validateId(id);

            boolean deleted = incidentDAO.delete(id);

            if (deleted) {
                LOGGER.info("Видалено інцидент ID = " + id);
            }

            return deleted;

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка видалення інциденту", e);
            throw e;
        }
    }

    // Обчислення інтегрального показника
    public double calculateIntegralScore(
            int f1, int f2, int f3) {

        validateFactor(f1, "F1");
        validateFactor(f2, "F2");
        validateFactor(f3, "F3");

        return 0.40 * f1
                + 0.35 * f2
                + 0.25 * f3;
    }

    // Зміна статусу інциденту
    public boolean changeStatus(int id, String newStatus)
            throws SQLException {

        try {
            validateId(id);

            Incident incident = incidentDAO.getById(id);

            if (incident == null) {
                throw new IllegalArgumentException(
                        "Інцидент з ID " + id + " не знайдено");
            }

            String currentStatus = incident.getStatus();

            if (!isValidTransition(currentStatus, newStatus)) {
                throw new IllegalArgumentException(
                        "Недопустимий перехід: "
                                + currentStatus + " -> " + newStatus);
            }

            incident.setStatus(newStatus);

            boolean updated = incidentDAO.update(incident);

            if (updated) {
                LOGGER.info("Статус інциденту ID = " + id
                        + " змінено: " + currentStatus
                        + " -> " + newStatus);
            }

            return updated;

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка зміни статусу інциденту", e);
            throw e;
        }
    }

    // Перевірка допустимих переходів
    private boolean isValidTransition(
            String currentStatus, String newStatus) {

        if (newStatus == null) {
            return false;
        }

        return switch (currentStatus) {
            case REPORTED -> CONFIRMED.equals(newStatus);
            case CONFIRMED -> RESPONDING.equals(newStatus);
            case RESPONDING -> CLOSED.equals(newStatus);
            default -> false;
        };
    }

    // Валідація даних інциденту
    private void validate(Incident incident) {

        if (incident == null) {
            throw new IllegalArgumentException(
                    "Інцидент не може бути null");
        }

        if (incident.getDescription() == null ||
                incident.getDescription().isBlank()) {
            throw new IllegalArgumentException(
                    "Опис інциденту обов'язковий");
        }

        validateFactor(incident.getF1(), "F1");
        validateFactor(incident.getF2(), "F2");
        validateFactor(incident.getF3(), "F3");
    }

    // Перевірка факторів 0-100
    private void validateFactor(int value, String name) {

        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(
                    name + " повинен бути від 0 до 100");
        }
    }

    // Перевірка пов'язаних сутностей
    private void validateRelations(Incident incident)
            throws SQLException {

        Route route = routeDAO.getById(incident.getRouteId());

        if (route == null) {
            throw new IllegalArgumentException(
                    "Маршрут не існує в базі даних");
        }

        Vehicle vehicle =
                vehicleDAO.getById(incident.getVehicleId());

        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Транспортний засіб не існує в базі даних");
        }

        if (vehicle.getRouteId() != null &&
                !vehicle.getRouteId().equals(incident.getRouteId())) {
            throw new IllegalArgumentException(
                    "Транспортний засіб не належить зазначеному маршруту");
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
