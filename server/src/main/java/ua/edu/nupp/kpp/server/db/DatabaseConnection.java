package ua.edu.nupp.kpp.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {

    private static final Logger LOGGER =
            Logger.getLogger(DatabaseConnection.class.getName());

    private static final String URL =
            "jdbc:mysql://localhost:3306/transport_incidents_v20"
                    + "?useSSL=false"
                    + "&serverTimezone=UTC"
                    + "&allowPublicKeyRetrieval=true"
                    + "&characterEncoding=UTF-8";

    private static final String USER =
            System.getenv().getOrDefault("DB_USER", "root");

    private static final String PASSWORD =
            System.getenv().getOrDefault("DB_PASSWORD", "");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            LOGGER.info(
                    "MySQL JDBC Driver успішно завантажено!"
            );

        } catch (ClassNotFoundException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Не вдалося завантажити MySQL JDBC Driver",
                    e
            );

            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection()
            throws SQLException {

        try {
            Connection connection =
                    DriverManager.getConnection(
                            URL,
                            USER,
                            PASSWORD
                    );

            LOGGER.info(
                    "Підключення до MySQL успішне!"
            );

            return connection;

        } catch (SQLException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Помилка підключення до MySQL",
                    e
            );

            throw e;
        }
    }
}
