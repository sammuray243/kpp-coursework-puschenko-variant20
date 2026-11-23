
package ua.edu.nupp.kpp.server.test;

import ua.edu.nupp.kpp.server.dao.RouteDAO;
import ua.edu.nupp.kpp.server.model.Route;

import java.sql.SQLException;

public class TestRouteDAO {

    public static void main(String[] args) {

        RouteDAO dao = new RouteDAO();

        try {
            System.out.println("=== ТЕСТ CRUD МАРШРУТІВ ===");

            // CREATE
            Route route = new Route(
                    0,
                    "99",
                    "Тестовий маршрут",
                    50
            );

            dao.create(route);

            System.out.println(
                    "CREATE: маршрут створено, ID = "
                            + route.getId()
            );

            // READ
            Route found = dao.getById(route.getId());

            if (found == null) {
                throw new SQLException(
                        "Створений маршрут не знайдено"
                );
            }

            System.out.println("READ: " + found);

            // UPDATE
            found.setRouteName(
                    "Оновлений тестовий маршрут"
            );
            found.setImportance(85);

            boolean updated = dao.update(found);

            System.out.println(
                    "UPDATE: " + updated
            );

            // Перевірка оновлених даних
            Route updatedRoute = dao.getById(
                    found.getId()
            );

            System.out.println(
                    "Після оновлення: " + updatedRoute
            );

            // DELETE
            boolean deleted = dao.delete(
                    route.getId()
            );

            System.out.println(
                    "DELETE: " + deleted
            );

            // Перевірка видалення
            Route afterDelete = dao.getById(
                    route.getId()
            );

            System.out.println(
                    "Запис після видалення: "
                            + afterDelete
            );

            System.out.println(
                    "=== ТЕСТ ЗАВЕРШЕНО ==="
            );

        } catch (SQLException e) {

            System.err.println(
                    "Помилка роботи з MySQL: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
