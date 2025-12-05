package dao;

import java.sql.*;

public class ActivityDAO {

    // ensure activity exists, return id
    public int ensureActivity(Connection conn, String name, double factor) throws SQLException {
        String find = "SELECT activity_id FROM ActivityType WHERE activity_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(find)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }

        String ins = "INSERT INTO ActivityType(activity_name, factor) VALUES (?,?) RETURNING activity_id";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setString(1, name);
            ps.setDouble(2, factor);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void upsertPlannedActivity(Connection conn, int instanceId, int activityId, double hours)
            throws SQLException {
        String sql = "INSERT INTO PlannedActivity(instance_id, activity_id, planned_hours) VALUES (?,?,?) ON CONFLICT (instance_id, activity_id) DO UPDATE SET planned_hours = EXCLUDED.planned_hours";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instanceId);
            ps.setInt(2, activityId);
            ps.setDouble(3, hours);
            ps.executeUpdate();
        }
    }
}
