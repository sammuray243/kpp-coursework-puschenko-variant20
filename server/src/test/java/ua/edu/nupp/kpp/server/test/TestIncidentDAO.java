
package ua.edu.nupp.kpp.server.test;

import ua.edu.nupp.kpp.server.dao.IncidentDAO;
import ua.edu.nupp.kpp.server.dao.RouteDAO;
import ua.edu.nupp.kpp.server.dao.VehicleDAO;
import ua.edu.nupp.kpp.server.model.Incident;
import ua.edu.nupp.kpp.server.model.Route;
import ua.edu.nupp.kpp.server.model.Vehicle;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class TestIncidentDAO {

    public static void main(String[] args) {

        IncidentDAO incidentDAO = new IncidentDAO();
        RouteDAO routeDAO = new RouteDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();

        int incidentId = 0;
        int vehicleId = 0;
        int routeId = 0;

        try {
            System.out.println(
                    "=== ТЕСТ CRUD ІНЦИДЕНТІВ ==="
            );

            // Створюємо тестовий маршрут
            Route route = new Route(
                    0,
                    "TEST-INC-20",
                    "Тестовий маршрут інциденту",
                    70
            );

            routeDAO.create(route);
            routeId = route.getId();

            // Створюємо тестовий транспорт
            Vehicle vehicle = new Vehicle(
                    0,
                    "TESTINC20",
                    "Автобус",
                    "Богдан А092",
                    routeId
            );

            vehicleDAO.create(vehicle);
            vehicleId = vehicle.getId();

            // CREATE
            Incident incident = new Incident(
                    0,
                    vehicleId,
                    routeId,
                    "Тестовий інцидент громадського транспорту",
                    LocalDateTime.now(),
                    "ПОВІДОМЛЕНО",
                    60,
                    70,
                    40,
                    58.5
            );

            incidentDAO.create(incident);
            incidentId = incident.getId();

            System.out.println(
                    "CREATE: інцидент створено, ID = "
                            + incidentId
            );

            // READ
            Incident found = incidentDAO.getById(incidentId);

            if (found == null) {
                throw new SQLException(
                        "Створений інцидент не знайдено!"
                );
            }

            System.out.println("READ: " + found);

            // UPDATE
            found.setDescription(
                    "Оновлений тестовий інцидент"
            );

            found.setF1(80);
            found.setF2(70);
            found.setF3(40);

            double score =
                    0.40 * found.getF1()
                            + 0.35 * found.getF2()
                            + 0.25 * found.getF3();

            found.setIntegralScore(score);

            boolean updated = incidentDAO.update(found);

            System.out.println("UPDATE: " + updated);

            // Перевірка оновлення
            Incident updatedIncident =
                    incidentDAO.getById(incidentId);

            System.out.println(
                    "Після оновлення: " + updatedIncident
            );

            // DELETE
            boolean deleted = incidentDAO.delete(incidentId);

            System.out.println("DELETE: " + deleted);

            // Перевірка видалення
            Incident afterDelete =
                    incidentDAO.getById(incidentId);

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

            // Видалення тестових даних
            try {
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
