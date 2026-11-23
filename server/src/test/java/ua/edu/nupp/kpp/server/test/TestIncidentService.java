
package ua.edu.nupp.kpp.server.test;

import ua.edu.nupp.kpp.server.dao.RouteDAO;
import ua.edu.nupp.kpp.server.dao.VehicleDAO;
import ua.edu.nupp.kpp.server.dao.IncidentDAO;

import ua.edu.nupp.kpp.server.model.Route;
import ua.edu.nupp.kpp.server.model.Vehicle;
import ua.edu.nupp.kpp.server.model.Incident;

import ua.edu.nupp.kpp.server.service.IncidentService;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class TestIncidentService {

    public static void main(String[] args) {

        RouteDAO routeDAO = new RouteDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        IncidentDAO incidentDAO = new IncidentDAO();
        IncidentService service = new IncidentService();

        int routeId = 0;
        int vehicleId = 0;
        int incidentId = 0;

        try {
            System.out.println(
                    "=== ТЕСТ INCIDENT SERVICE ==="
            );

            // 1. Перевірка інтегрального показника
            double score =
                    service.calculateIntegralScore(80, 70, 40);

            check(Math.abs(score - 66.5) < 0.000001,
                    "Неправильний інтегральний показник");

            System.out.println(
                    "PASS: інтегральний показник = " + score
            );

            // 2. Перевірка валідації факторів
            boolean invalidFactorRejected = false;

            try {
                service.calculateIntegralScore(120, 50, 30);
            } catch (IllegalArgumentException e) {
                invalidFactorRejected = true;
                System.out.println(
                        "PASS: некоректний F1 відхилено"
                );
            }

            check(invalidFactorRejected,
                    "Некоректний фактор не відхилено");

            // 3. Створення тестового маршруту
            Route route = new Route(
                    0,
                    "TEST-SERVICE-20",
                    "Маршрут тестування Service",
                    70
            );

            routeDAO.create(route);
            routeId = route.getId();

            // 4. Створення тестового транспорту
            Vehicle vehicle = new Vehicle(
                    0,
                    "TESTSERV20",
                    "Автобус",
                    "Богдан А092",
                    routeId
            );

            vehicleDAO.create(vehicle);
            vehicleId = vehicle.getId();

            // 5. Створення інциденту через Service
            Incident incident = new Incident(
                    0,
                    vehicleId,
                    routeId,
                    "Тестування життєвого циклу інциденту",
                    LocalDateTime.now(),
                    "ПОВІДОМЛЕНО",
                    80,
                    70,
                    40,
                    0.0
            );

            service.create(incident);
            incidentId = incident.getId();

            Incident created = service.getById(incidentId);

            check(created != null,
                    "Інцидент не створено");

            check("ПОВІДОМЛЕНО".equals(created.getStatus()),
                    "Неправильний початковий статус");

            check(Math.abs(
                            created.getIntegralScore() - 66.5
                    ) < 0.000001,
                    "Неправильний показник у MySQL");

            System.out.println(
                    "PASS: інцидент створено, ID = " + incidentId
            );

            // 6. Заборонений перехід
            boolean invalidTransitionRejected = false;

            try {
                service.changeStatus(incidentId, "ЗАКРИТО");
            } catch (IllegalArgumentException e) {
                invalidTransitionRejected = true;

                System.out.println(
                        "PASS: перехід ПОВІДОМЛЕНО -> ЗАКРИТО заборонено"
                );
            }

            check(invalidTransitionRejected,
                    "Недопустимий перехід не заблоковано");

            // Переконуємося, що статус у БД не змінився
            check(
                    "ПОВІДОМЛЕНО".equals(
                            service.getById(incidentId).getStatus()
                    ),
                    "Статус змінився після забороненого переходу"
            );

            // 7. ПОВІДОМЛЕНО -> ПІДТВЕРДЖЕНО
            check(
                    service.changeStatus(
                            incidentId, "ПІДТВЕРДЖЕНО"),
                    "Помилка підтвердження інциденту"
            );

            checkStatus(service, incidentId, "ПІДТВЕРДЖЕНО");

            System.out.println(
                    "PASS: ПОВІДОМЛЕНО -> ПІДТВЕРДЖЕНО"
            );

            // 8. ПІДТВЕРДЖЕНО -> РЕАГУВАННЯ
            check(
                    service.changeStatus(
                            incidentId, "РЕАГУВАННЯ"),
                    "Помилка початку реагування"
            );

            checkStatus(service, incidentId, "РЕАГУВАННЯ");

            System.out.println(
                    "PASS: ПІДТВЕРДЖЕНО -> РЕАГУВАННЯ"
            );

            // 9. РЕАГУВАННЯ -> ЗАКРИТО
            check(
                    service.changeStatus(
                            incidentId, "ЗАКРИТО"),
                    "Помилка закриття інциденту"
            );

            checkStatus(service, incidentId, "ЗАКРИТО");

            System.out.println(
                    "PASS: РЕАГУВАННЯ -> ЗАКРИТО"
            );

            // 10. Заборона переходів із ЗАКРИТО
            boolean closedTransitionRejected = false;

            try {
                service.changeStatus(
                        incidentId, "ПОВІДОМЛЕНО");
            } catch (IllegalArgumentException e) {
                closedTransitionRejected = true;
            }

            check(closedTransitionRejected,
                    "Закритий інцидент дозволяє зміну статусу");

            checkStatus(service, incidentId, "ЗАКРИТО");

            System.out.println(
                    "PASS: закритий інцидент не змінює статус"
            );

            // 11. Заборона обходу життєвого циклу
            Incident edited = service.getById(incidentId);
            edited.setStatus("ПОВІДОМЛЕНО");

            boolean directUpdateRejected = false;

            try {
                service.update(edited);
            } catch (IllegalArgumentException e) {
                directUpdateRejected = true;
            }

            check(directUpdateRejected,
                    "update() дозволив обійти життєвий цикл");

            checkStatus(service, incidentId, "ЗАКРИТО");

            System.out.println(
                    "PASS: update() не дозволяє обійти життєвий цикл"
            );

            System.out.println(
                    "=== УСІ ТЕСТИ УСПІШНО ПРОЙДЕНО ==="
            );

        } catch (SQLException | RuntimeException e) {

            System.err.println(
                    "ТЕСТ НЕ ПРОЙДЕНО: " + e.getMessage()
            );

            e.printStackTrace();

        } finally {

            // Очищення тестових даних
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

    // Перевірка умови тесту
    private static void check(
            boolean condition, String message) {

        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    // Перевірка статусу безпосередньо з MySQL
    private static void checkStatus(
            IncidentService service,
            int id,
            String expected
    ) throws SQLException {

        Incident incident = service.getById(id);

        check(incident != null,
                "Інцидент зник із бази даних");

        check(expected.equals(incident.getStatus()),
                "Очікувався статус " + expected
                        + ", отримано " + incident.getStatus());
    }
}
