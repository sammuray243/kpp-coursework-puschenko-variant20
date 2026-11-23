
package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.ResponseTaskDAO;
import ua.edu.nupp.kpp.server.dao.IncidentDAO;

import ua.edu.nupp.kpp.server.model.ResponseTask;
import ua.edu.nupp.kpp.server.model.Incident;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResponseTaskService {

    private static final Logger LOGGER =
            Logger.getLogger(ResponseTaskService.class.getName());

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("НОВЕ", "ВИКОНУЄТЬСЯ", "ВИКОНАНО");

    private final ResponseTaskDAO taskDAO;
    private final IncidentDAO incidentDAO;

    public ResponseTaskService() {
        this.taskDAO = new ResponseTaskDAO();
        this.incidentDAO = new IncidentDAO();
    }

    // CREATE - створення завдання реагування
    public void create(ResponseTask task) throws SQLException {
        try {
            validate(task);
            validateIncident(task.getIncidentId());

            taskDAO.create(task);

            LOGGER.info("Створено завдання реагування ID = "
                    + task.getId());

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка створення завдання реагування", e);
            throw e;
        }
    }

    // READ - отримання завдання за ID
    public ResponseTask getById(int id) throws SQLException {
        try {
            validateId(id);

            return taskDAO.getById(id);

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання завдання реагування", e);
            throw e;
        }
    }

    // READ - отримання всіх завдань
    public List<ResponseTask> getAll() throws SQLException {
        try {
            return taskDAO.getAll();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання списку завдань", e);
            throw e;
        }
    }

    // READ - завдання конкретного інциденту
    public List<ResponseTask> getByIncidentId(int incidentId)
            throws SQLException {

        try {
            validateIncident(incidentId);

            return taskDAO.getByIncidentId(incidentId);

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка отримання завдань інциденту", e);
            throw e;
        }
    }

    // UPDATE - редагування завдання
    public boolean update(ResponseTask task) throws SQLException {
        try {
            validate(task);
            validateId(task.getId());
            validateIncident(task.getIncidentId());

            boolean updated = taskDAO.update(task);

            if (updated) {
                LOGGER.info("Оновлено завдання реагування ID = "
                        + task.getId());
            }

            return updated;

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка оновлення завдання реагування", e);
            throw e;
        }
    }

    // DELETE - видалення завдання
    public boolean delete(int id) throws SQLException {
        try {
            validateId(id);

            boolean deleted = taskDAO.delete(id);

            if (deleted) {
                LOGGER.info("Видалено завдання реагування ID = "
                        + id);
            }

            return deleted;

        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                    "Помилка видалення завдання реагування", e);
            throw e;
        }
    }

    // Перевірка даних завдання
    private void validate(ResponseTask task) {

        if (task == null) {
            throw new IllegalArgumentException(
                    "Завдання реагування не може бути null");
        }

        if (task.getTaskDescription() == null ||
                task.getTaskDescription().isBlank()) {
            throw new IllegalArgumentException(
                    "Опис завдання обов'язковий");
        }

        if (task.getResponsiblePerson() == null ||
                task.getResponsiblePerson().isBlank()) {
            throw new IllegalArgumentException(
                    "Відповідальна особа обов'язкова");
        }

        if (task.getTaskStatus() == null ||
                !ALLOWED_STATUSES.contains(task.getTaskStatus())) {
            throw new IllegalArgumentException(
                    "Недопустимий статус завдання. " +
                            "Дозволено: НОВЕ, ВИКОНУЄТЬСЯ, ВИКОНАНО");
        }
    }

    // Перевірка існування інциденту
    private void validateIncident(int incidentId)
            throws SQLException {

        validateId(incidentId);

        Incident incident = incidentDAO.getById(incidentId);

        if (incident == null) {
            throw new IllegalArgumentException(
                    "Інцидент з ID " + incidentId +
                            " не існує в базі даних");
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
