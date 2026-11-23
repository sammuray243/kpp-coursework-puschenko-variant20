
package ua.edu.nupp.kpp.server.test;

import ua.edu.nupp.kpp.server.model.Route;
import ua.edu.nupp.kpp.server.model.Vehicle;
import ua.edu.nupp.kpp.server.model.Incident;
import ua.edu.nupp.kpp.server.model.ResponseTask;

import ua.edu.nupp.kpp.server.service.RouteService;
import ua.edu.nupp.kpp.server.service.VehicleService;
import ua.edu.nupp.kpp.server.service.IncidentService;
import ua.edu.nupp.kpp.server.service.ResponseTaskService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class TestServiceValidation {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        RouteService routeService = new RouteService();
        VehicleService vehicleService = new VehicleService();
        IncidentService incidentService = new IncidentService();
        ResponseTaskService taskService =
                new ResponseTaskService();

        int routeId = 0;
        int vehicleId = 0;
        int incidentId = 0;
        int taskId = 0;

        System.out.println(
                "=== ТЕСТ ВАЛІДАЦІЇ SERVICE-РІВНЯ ==="
        );

        try {
            // 1. ROUTE SERVICE

            System.out.println("\n=== ROUTE SERVICE ===");

            expectInvalid("Порожній номер маршруту", () -> {
                routeService.create(new Route(
                        0, "", "Тестовий маршрут", 50
                ));
            });

            expectInvalid("Порожня назва маршруту", () -> {
                routeService.create(new Route(
                        0, "99", "", 50
                ));
            });

            expectInvalid("Важливість більше 100", () -> {
                routeService.create(new Route(
                        0, "99", "Тестовий маршрут", 150
                ));
            });

            expectInvalid("Від'ємна важливість", () -> {
                routeService.create(new Route(
                        0, "99", "Тестовий маршрут", -10
                ));
            });

            // Створення правильного маршруту
            String suffix = UUID.randomUUID()
                    .toString().substring(0, 8);

            Route route = new Route(
                    0,
                    "T" + suffix,
                    "Маршрут перевірки Service",
                    70
            );

            routeService.create(route);
            routeId = route.getId();

            check("Створення правильного маршруту",
                    routeId > 0);

            // 2. VEHICLE SERVICE

            System.out.println("\n=== VEHICLE SERVICE ===");

            final int validRouteId = routeId;

            expectInvalid("Порожній номер транспорту", () -> {
                vehicleService.create(new Vehicle(
                        0, "", "Автобус",
                        "Богдан А092", validRouteId
                ));
            });

            expectInvalid("Порожній тип транспорту", () -> {
                vehicleService.create(new Vehicle(
                        0, "TEST123", "",
                        "Богдан А092", validRouteId
                ));
            });

            expectInvalid("Порожня модель транспорту", () -> {
                vehicleService.create(new Vehicle(
                        0, "TEST123", "Автобус",
                        "", validRouteId
                ));
            });

            expectInvalid("Неіснуючий маршрут", () -> {
                vehicleService.create(new Vehicle(
                        0, "TEST123", "Автобус",
                        "Богдан А092", Integer.MAX_VALUE
                ));
            });

            // Створення правильного транспорту
            Vehicle vehicle = new Vehicle(
                    0,
                    "V" + suffix,
                    "Автобус",
                    "Богдан А092",
                    routeId
            );

            vehicleService.create(vehicle);
            vehicleId = vehicle.getId();

            check("Створення правильного транспорту",
                    vehicleId > 0);

            // 3. INCIDENT SERVICE

            System.out.println("\n=== INCIDENT SERVICE ===");

            Incident incident = new Incident(
                    0,
                    vehicleId,
                    routeId,
                    "Тестовий інцидент для перевірки",
                    LocalDateTime.now(),
                    "ПОВІДОМЛЕНО",
                    80,
                    70,
                    40,
                    0.0
            );

            incidentService.create(incident);
            incidentId = incident.getId();

            check("Створення тестового інциденту",
                    incidentId > 0);

            // 4. RESPONSE TASK SERVICE

            System.out.println(
                    "\n=== RESPONSE TASK SERVICE ==="
            );

            final int validIncidentId = incidentId;

            expectInvalid("Порожній опис завдання", () -> {
                taskService.create(new ResponseTask(
                        0, validIncidentId,
                        "", "Диспетчер", "НОВЕ"
                ));
            });

            expectInvalid("Порожня відповідальна особа", () -> {
                taskService.create(new ResponseTask(
                        0, validIncidentId,
                        "Перевірити автобус",
                        "", "НОВЕ"
                ));
            });

            expectInvalid("Неправильний статус", () -> {
                taskService.create(new ResponseTask(
                        0, validIncidentId,
                        "Перевірити автобус",
                        "Диспетчер", "НЕВІДОМИЙ"
                ));
            });

            expectInvalid("Неіснуючий інцидент", () -> {
                taskService.create(new ResponseTask(
                        0, Integer.MAX_VALUE,
                        "Перевірити автобус",
                        "Диспетчер", "НОВЕ"
                ));
            });

            // Створення правильного завдання
            ResponseTask task = new ResponseTask(
                    0,
                    incidentId,
                    "Перевірити технічний стан автобуса",
                    "Черговий механік",
                    "НОВЕ"
            );

            taskService.create(task);
            taskId = task.getId();

            check("Створення правильного завдання",
                    taskId > 0);

            // Перевірка читання
            check("Читання маршруту",
                    routeService.getById(routeId) != null);

            check("Читання транспорту",
                    vehicleService.getById(vehicleId) != null);

            check("Читання завдання",
                    taskService.getById(taskId) != null);

        } catch (Exception e) {

            failed++;
            System.err.println(
                    "FAIL: Неочікувана помилка: "
                            + e.getMessage()
            );
            e.printStackTrace();

        } finally {

            // Видаляємо тестові записи
            // у зворотному порядку залежностей

            System.out.println(
                    "\n=== ОЧИЩЕННЯ ТЕСТОВИХ ДАНИХ ==="
            );

            try {
                if (taskId > 0) {
                    taskService.delete(taskId);
                }
            } catch (Exception e) {
                cleanupError("завдання", e);
            }

            try {
                if (incidentId > 0) {
                    incidentService.delete(incidentId);
                }
            } catch (Exception e) {
                cleanupError("інциденту", e);
            }

            try {
                if (vehicleId > 0) {
                    vehicleService.delete(vehicleId);
                }
            } catch (Exception e) {
                cleanupError("транспорту", e);
            }

            try {
                if (routeId > 0) {
                    routeService.delete(routeId);
                }
            } catch (Exception e) {
                cleanupError("маршруту", e);
            }
        }

        System.out.println(
                "\n=== РЕЗУЛЬТАТИ ТЕСТУВАННЯ ==="
        );

        System.out.println("Успішно: " + passed);
        System.out.println("Помилок: " + failed);

        if (failed == 0) {
            System.out.println(
                    "=== УСІ ТЕСТИ УСПІШНО ПРОЙДЕНО ==="
            );
        } else {
            System.err.println(
                    "=== Є ПОМИЛКИ ТЕСТУВАННЯ ==="
            );
            System.exit(1);
        }
    }

    // Функціональний інтерфейс для тестів
    @FunctionalInterface
    private interface TestAction {
        void run() throws SQLException;
    }

    // Перевірка відхилення неправильних даних
    private static void expectInvalid(
            String name, TestAction action) {

        try {
            action.run();

            failed++;
            System.err.println(
                    "FAIL: " + name
                            + " - неправильні дані прийнято"
            );

        } catch (IllegalArgumentException e) {

            passed++;
            System.out.println(
                    "PASS: " + name + " - відхилено"
            );

        } catch (Exception e) {

            failed++;
            System.err.println(
                    "FAIL: " + name
                            + " - неочікувана помилка: "
                            + e.getMessage()
            );
        }
    }

    // Перевірка правильного результату
    private static void check(
            String name, boolean condition) {

        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.err.println("FAIL: " + name);
        }
    }

    // Помилка очищення
    private static void cleanupError(
            String name, Exception e) {

        failed++;
        System.err.println(
                "Помилка очищення " + name
                        + ": " + e.getMessage()
        );
    }
}
