
package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.db.DatabaseConnection;
import ua.edu.nupp.kpp.server.model.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RouteDAO {

    // CREATE - додавання нового маршруту
    public void create(Route route) throws SQLException {

        String sql = "INSERT INTO routes " +
                "(route_number, route_name, importance) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, route.getRouteNumber());
            ps.setString(2, route.getRouteName());
            ps.setInt(3, route.getImportance());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    route.setId(keys.getInt(1));
                }
            }
        }
    }

    // READ - отримання маршруту за ID
    public Route getById(int id) throws SQLException {

        String sql = "SELECT * FROM routes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    // READ - отримання всіх маршрутів
    public List<Route> getAll() throws SQLException {

        List<Route> routes = new ArrayList<>();

        String sql = "SELECT * FROM routes ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                routes.add(mapRow(rs));
            }
        }

        return routes;
    }

    // UPDATE - редагування маршруту
    public boolean update(Route route) throws SQLException {

        String sql = "UPDATE routes SET " +
                "route_number = ?, " +
                "route_name = ?, " +
                "importance = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, route.getRouteNumber());
            ps.setString(2, route.getRouteName());
            ps.setInt(3, route.getImportance());
            ps.setInt(4, route.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // DELETE - видалення маршруту
    public boolean delete(int id) throws SQLException {

        String sql = "DELETE FROM routes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Перетворення ResultSet на об'єкт Route
    private Route mapRow(ResultSet rs) throws SQLException {

        return new Route(
                rs.getInt("id"),
                rs.getString("route_number"),
                rs.getString("route_name"),
                rs.getInt("importance")
        );
    }
}
