
package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.db.DatabaseConnection;
import ua.edu.nupp.kpp.server.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    // CREATE - додавання транспортного засобу
    public void create(Vehicle vehicle) throws SQLException {

        String sql = "INSERT INTO vehicles " +
                "(registration_number, vehicle_type, model, route_id) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, vehicle.getRegistrationNumber());
            ps.setString(2, vehicle.getVehicleType());
            ps.setString(3, vehicle.getModel());

            if (vehicle.getRouteId() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, vehicle.getRouteId());
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    vehicle.setId(keys.getInt(1));
                }
            }
        }
    }

    // READ - пошук транспортного засобу за ID
    public Vehicle getById(int id) throws SQLException {

        String sql = "SELECT * FROM vehicles WHERE id = ?";

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

    // READ - отримання всіх транспортних засобів
    public List<Vehicle> getAll() throws SQLException {

        List<Vehicle> vehicles = new ArrayList<>();

        String sql = "SELECT * FROM vehicles ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                vehicles.add(mapRow(rs));
            }
        }

        return vehicles;
    }

    // UPDATE - редагування транспортного засобу
    public boolean update(Vehicle vehicle) throws SQLException {

        String sql = "UPDATE vehicles SET " +
                "registration_number = ?, " +
                "vehicle_type = ?, " +
                "model = ?, " +
                "route_id = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, vehicle.getRegistrationNumber());
            ps.setString(2, vehicle.getVehicleType());
            ps.setString(3, vehicle.getModel());

            if (vehicle.getRouteId() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, vehicle.getRouteId());
            }

            ps.setInt(5, vehicle.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // DELETE - видалення транспортного засобу
    public boolean delete(int id) throws SQLException {

        String sql = "DELETE FROM vehicles WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Перетворення ResultSet на об'єкт Vehicle
    private Vehicle mapRow(ResultSet rs) throws SQLException {

        int routeId = rs.getInt("route_id");

        Integer nullableRouteId =
                rs.wasNull() ? null : routeId;

        return new Vehicle(
                rs.getInt("id"),
                rs.getString("registration_number"),
                rs.getString("vehicle_type"),
                rs.getString("model"),
                nullableRouteId
        );
    }
}
