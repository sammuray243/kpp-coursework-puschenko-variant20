
package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.db.DatabaseConnection;
import ua.edu.nupp.kpp.server.model.ResponseTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResponseTaskDAO {

    // CREATE - додавання завдання реагування
    public void create(ResponseTask task) throws SQLException {

        String sql = "INSERT INTO response_tasks " +
                "(incident_id, task_description, " +
                "responsible_person, task_status) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, task.getIncidentId());
            ps.setString(2, task.getTaskDescription());
            ps.setString(3, task.getResponsiblePerson());
            ps.setString(4, task.getTaskStatus());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                }
            }
        }
    }

    // READ - отримання завдання за ID
    public ResponseTask getById(int id) throws SQLException {

        String sql = "SELECT * FROM response_tasks WHERE id = ?";

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

    // READ - отримання всіх завдань реагування
    public List<ResponseTask> getAll() throws SQLException {

        List<ResponseTask> tasks = new ArrayList<>();

        String sql = "SELECT * FROM response_tasks ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tasks.add(mapRow(rs));
            }
        }

        return tasks;
    }

    // READ - отримання завдань певного інциденту
    public List<ResponseTask> getByIncidentId(int incidentId)
            throws SQLException {

        List<ResponseTask> tasks = new ArrayList<>();

        String sql = "SELECT * FROM response_tasks " +
                "WHERE incident_id = ? ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, incidentId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    tasks.add(mapRow(rs));
                }
            }
        }

        return tasks;
    }

    // UPDATE - редагування завдання реагування
    public boolean update(ResponseTask task) throws SQLException {

        String sql = "UPDATE response_tasks SET " +
                "incident_id = ?, " +
                "task_description = ?, " +
                "responsible_person = ?, " +
                "task_status = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, task.getIncidentId());
            ps.setString(2, task.getTaskDescription());
            ps.setString(3, task.getResponsiblePerson());
            ps.setString(4, task.getTaskStatus());
            ps.setInt(5, task.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // DELETE - видалення завдання реагування
    public boolean delete(int id) throws SQLException {

        String sql = "DELETE FROM response_tasks WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Перетворення ResultSet на об'єкт ResponseTask
    private ResponseTask mapRow(ResultSet rs)
            throws SQLException {

        return new ResponseTask(
                rs.getInt("id"),
                rs.getInt("incident_id"),
                rs.getString("task_description"),
                rs.getString("responsible_person"),
                rs.getString("task_status")
        );
    }
}
