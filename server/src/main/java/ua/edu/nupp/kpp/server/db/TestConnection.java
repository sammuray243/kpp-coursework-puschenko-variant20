package ua.edu.nupp.kpp.server.db;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {

    public static void main(String[] args) {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            System.out.println(
                    "Підключення до MySQL успішне!"
            );

            System.out.println(
                    "База даних: transport_incidents_v20"
            );

        } catch (SQLException e) {

            System.out.println(
                    "Помилка підключення до MySQL!"
            );

            e.printStackTrace();
        }
    }
}
