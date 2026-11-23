
package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.db.DatabaseConnection;
import ua.edu.nupp.kpp.server.model.Incident;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidentDAO {

    // CREATE - додавання інциденту
    public void create(Incident incident) throws SQLException {

        String sql = """
                INSERT INTO incidents
                (vehicle_id, route_id, description,
                 incident_date, status, f1, f2, f3,
                 integral_score)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            setParameters(ps, incident);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    incident.setId(keys.getInt(1));
                }
            }
        }
    }

    // READ - пошук інциденту за ID
    public Incident getById(int id) throws SQLException {

        String sql = "SELECT * FROM incidents WHERE id = ?";

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

    // READ - отримання всіх інцидентів
    public List<Incident> getAll() throws SQLException {

        List<Incident> incidents = new ArrayList<>();

        String sql = "SELECT * FROM incidents ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                incidents.add(mapRow(rs));
            }
        }

        return incidents;
    }

    // UPDATE - редагування інциденту
    public boolean update(Incident incident) throws SQLException {

        String sql = """
                UPDATE incidents
                SET vehicle_id = ?,
                    route_id = ?,
                    description = ?,
                    incident_date = ?,
                    status = ?,
                    f1 = ?,
                    f2 = ?,
                    f3 = ?,
                    integral_score = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, incident);
            ps.setInt(10, incident.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // DELETE - видалення інциденту
    public boolean delete(int id) throws SQLException {

        String sql = "DELETE FROM incidents WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Передавання параметрів SQL
    private void setParameters(
            PreparedStatement ps,
            Incident incident
    ) throws SQLException {

        ps.setInt(1, incident.getVehicleId());
        ps.setInt(2, incident.getRouteId());
        ps.setString(3, incident.getDescription());

        if (incident.getIncidentDate() == null) {
            ps.setNull(4, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(
                    4,
                    Timestamp.valueOf(incident.getIncidentDate())
            );
        }

        ps.setString(5, incident.getStatus());
        ps.setInt(6, incident.getF1());
        ps.setInt(7, incident.getF2());
        ps.setInt(8, incident.getF3());
        ps.setDouble(9, incident.getIntegralScore());
    }

    // Перетворення ResultSet на Incident
    private Incident mapRow(ResultSet rs) throws SQLException {

        Timestamp timestamp = rs.getTimestamp("incident_date");

        return new Incident(
                rs.getInt("id"),
                rs.getInt("vehicle_id"),
                rs.getInt("route_id"),
                rs.getString("description"),
                timestamp == null
                        ? null
                        : timestamp.toLocalDateTime(),
                rs.getString("status"),
                rs.getInt("f1"),
                rs.getInt("f2"),
                rs.getInt("f3"),
                rs.getDouble("integral_score")
        );
    }
}
