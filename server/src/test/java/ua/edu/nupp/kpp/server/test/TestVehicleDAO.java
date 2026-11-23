
package ua.edu.nupp.kpp.server.test;

import ua.edu.nupp.kpp.server.dao.VehicleDAO;
import ua.edu.nupp.kpp.server.model.Vehicle;

import java.sql.SQLException;

public class TestVehicleDAO {

    public static void main(String[] args) {

        VehicleDAO dao = new VehicleDAO();

        try {
            System.out.println(
                    "=== ТЕСТ CRUD ТРАНСПОРТНИХ ЗАСОБІВ ==="
            );

            // CREATE - створення транспортного засобу
            Vehicle vehicle = new Vehicle(
                    0,
                    "TEST2026AA",
                    "Автобус",
                    "Богдан А092",
                    1
            );

            dao.create(vehicle);

            System.out.println(
                    "CREATE: транспорт створено, ID = "
                            + vehicle.getId()
            );

            // READ - отримання транспортного засобу
            Vehicle found = dao.getById(vehicle.getId());

            if (found == null) {
                throw new SQLException(
                        "Транспортний засіб не знайдено!"
                );
            }

            System.out.println("READ: " + found);

            // UPDATE - редагування транспортного засобу
            found.setVehicleType("Тролейбус");
            found.setModel("Богдан Т701");

            boolean updated = dao.update(found);

            System.out.println("UPDATE: " + updated);

            // Перевірка оновлення
            Vehicle updatedVehicle =
                    dao.getById(found.getId());

            System.out.println(
                    "Після оновлення: " + updatedVehicle
            );

            // DELETE - видалення транспортного засобу
            boolean deleted = dao.delete(vehicle.getId());

            System.out.println("DELETE: " + deleted);

            // Перевірка видалення
            Vehicle afterDelete =
                    dao.getById(vehicle.getId());

            System.out.println(
                    "Запис після видалення: " + afterDelete
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
