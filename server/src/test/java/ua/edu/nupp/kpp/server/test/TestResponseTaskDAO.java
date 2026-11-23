
package ua.edu.nupp.kpp.server.test;

import ua.edu.nupp.kpp.server.dao.IncidentDAO;
import ua.edu.nupp.kpp.server.dao.ResponseTaskDAO;
import ua.edu.nupp.kpp.server.dao.RouteDAO;
import ua.edu.nupp.kpp.server.dao.VehicleDAO;

import ua.edu.nupp.kpp.server.model.Incident;
import ua.edu.nupp.kpp.server.model.ResponseTask;
import ua.edu.nupp.kpp.server.model.Route;
import ua.edu.nupp.kpp.server.model.Vehicle;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class TestResponseTaskDAO {

    public static void main(String[] args) {

        RouteDAO routeDAO = new RouteDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        IncidentDAO incidentDAO = new IncidentDAO();
        ResponseTaskDAO taskDAO = new ResponseTaskDAO();

        int routeId = 0;
        int vehicleId = 0;
        int incidentId = 0;
        int taskId = 0;

        try {
            System.out.println(
                    "=== ТЕСТ CRUD ЗАВДАНЬ РЕАГУВАННЯ ==="
            );

            // Створюємо тестовий маршрут
            Route route = new Route(
                    0,
                    "TEST-RT-20",
                    "Маршрут для перевірки завдань",
                    70
            );

            routeDAO.create(route);
            routeId = route.getId();

            // Створюємо тестовий транспорт
            Vehicle vehicle = new Vehicle(
                    0,
                    "TESTRT20",
                    "Автобус",
                    "Богдан А092",
                    routeId
            );

            vehicleDAO.create(vehicle);
            vehicleId = vehicle.getId();

            // Створюємо тестовий інцидент
            Incident incident = new Incident(
                    0,
                    vehicleId,
                    routeId,
                    "Тестовий інцидент для завдання",
                    LocalDateTime.now(),
                    "ПОВІДОМЛЕНО",
                    60,
                    70,
                    40,
                    58.5
            );

            incidentDAO.create(incident);
            incidentId = incident.getId();

            // CREATE
            ResponseTask task = new ResponseTask(
                    0,
                    incidentId,
                    "Перевірити технічний стан автобуса",
                    "Тестовий диспетчер",
                    "НОВЕ"
            );

            taskDAO.create(task);
            taskId = task.getId();

            System.out.println(
                    "CREATE: завдання створено, ID = "
                            + taskId
            );

            // READ
            ResponseTask found = taskDAO.getById(taskId);

            if (found == null) {
                throw new SQLException(
                        "Створене завдання не знайдено!"
                );
            }

            System.out.println("READ: " + found);

            // UPDATE
            found.setTaskDescription(
                    "Провести технічну діагностику автобуса"
            );

            found.setResponsiblePerson(
                    "Черговий механік"
            );

            found.setTaskStatus("ВИКОНУЄТЬСЯ");

            boolean updated = taskDAO.update(found);

            System.out.println("UPDATE: " + updated);

            // Перевірка оновлення
            ResponseTask updatedTask =
                    taskDAO.getById(taskId);

            System.out.println(
                    "Після оновлення: " + updatedTask
            );

            // DELETE
            boolean deleted = taskDAO.delete(taskId);

            System.out.println("DELETE: " + deleted);

            // Перевірка видалення
            ResponseTask afterDelete =
                    taskDAO.getById(taskId);

            System.out.println(
                    "Запис після видалення: " + afterDelete
            );

            System.out.println(
                    "=== ТЕСТ ЗАВЕРШЕНО ==="
            );

        } catch (SQLException e) {

            System.err.println(
                    "Помилка MySQL: " + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            // Видаляємо тестові записи у зворотному
            // порядку залежностей зовнішніх ключів
            try {
                if (taskId > 0) {
                    taskDAO.delete(taskId);
                }

                if (incidentId > 0) {
                    incidentDAO.delete(incidentId);
                }

                if (vehicleId > 0) {
                    vehicleDAO.delete(vehicleId);
                }

                if (routeId > 0) {
                    routeDAO.delete(routeId);
                }

                System.out.println(
                        "Очищення тестових даних завершено."
                );

            } catch (SQLException e) {
                System.err.println(
                        "Помилка очищення: " + e.getMessage()
                );
            }
        }
    }
}
