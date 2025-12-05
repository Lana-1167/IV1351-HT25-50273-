package dao;

import java.sql.*;

public class AllocationDAO {

    // Count distinct instances for teacher in same year+period
    public int countDistinctInstancesForTeacherInPeriod(int empId, int year, String period) {
        String sql = "SELECT COUNT(DISTINCT a.instance_id) FROM Allocation a JOIN CourseInstance ci ON a.instance_id = ci.instance_id "
                +
                "WHERE a.emp_id = ? AND ci.year = ? AND ci.period = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("Error counting allocations: " + e.getMessage());
        }
        return 0;
    }

    // allocate (used by CourseService with transaction control)
    public void insertAllocation(Connection conn, int empId, int instanceId, int activityId, double hours)
            throws SQLException {
        String sql = "INSERT INTO Allocation(emp_id, instance_id, activity_id, allocated_hours) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setInt(2, instanceId);
            ps.setInt(3, activityId);
            ps.setDouble(4, hours);
            ps.executeUpdate();
        }
    }

    public boolean deleteAllocation(int empId, int instanceId, int activityId) throws SQLException {
        String sql = "DELETE FROM Allocation WHERE emp_id = ? AND instance_id = ? AND activity_id = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setInt(2, instanceId);
            ps.setInt(3, activityId);
            return ps.executeUpdate() > 0;
        }
    }

    // sum actual allocated hours*factor for an instance (used for cost)
    public double sumActualHoursForInstance(int instanceId) {
        String sql = "SELECT COALESCE(SUM(a.allocated_hours * at.factor),0) FROM Allocation a JOIN ActivityType at ON a.activity_id = at.activity_id WHERE a.instance_id = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, instanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (Exception e) {
            System.out.println("Error sumActualHours: " + e.getMessage());
        }
        return 0.0;
    }
}
