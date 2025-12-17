package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.ExerciseAllocationInfo;

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

    public ResultSet getAllocationsForTeacher(Connection c, int empId) throws SQLException {
        String sql = "SELECT cl.course_code, ci.instance_id, ci.period, ci.year, " +
                "at.activity_name, a.allocated_hours " +
                "FROM Allocation a " +
                "JOIN CourseInstance ci ON a.instance_id = ci.instance_id " +
                "JOIN CourseLayout cl ON ci.layout_id = cl.layout_id " +
                "JOIN ActivityType at ON a.activity_id = at.activity_id " +
                "WHERE a.emp_id = ? " +
                "ORDER BY ci.year, ci.period";
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setInt(1, empId);
        return ps.executeQuery();
    }

    public List<ExerciseAllocationInfo> getExerciseAllocationsForTeacher(int empId) {

        List<ExerciseAllocationInfo> list = new ArrayList<>();

        String sql = "SELECT e.emp_id, e.first_name, e.last_name, " +
                "       cl.course_code, ci.instance_id, ci.year, ci.period, " +
                "       a.allocated_hours " +
                "FROM Allocation a " +
                "JOIN Employee e ON a.emp_id = e.emp_id " +
                "JOIN CourseInstance ci ON a.instance_id = ci.instance_id " +
                "JOIN CourseLayout cl ON ci.layout_id = cl.layout_id " +
                "JOIN ActivityType at ON a.activity_id = at.activity_id " +
                "WHERE at.activity_name = 'Exercise' AND e.emp_id = ? " +
                "ORDER BY ci.year, ci.period";

        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, empId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ExerciseAllocationInfo(
                            rs.getInt("emp_id"),
                            rs.getString("first_name") + " " + rs.getString("last_name"),
                            rs.getString("course_code"),
                            rs.getInt("instance_id"),
                            rs.getInt("year"),
                            rs.getString("period"),
                            rs.getDouble("allocated_hours")));
                }
            }

        } catch (Exception e) {
            System.out.println("Exercise allocation query error: " + e.getMessage());
        }

        return list;
    }

    public boolean hasAnyAllocationForInstance(
            Connection c, int empId, int instanceId) throws Exception {

        String sql = "SELECT 1 FROM Allocation " +
                "WHERE emp_id = ? AND instance_id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setInt(2, instanceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

}
