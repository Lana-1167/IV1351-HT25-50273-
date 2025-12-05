package dao;

import model.TeachingCost;
import java.sql.*;

public class TeachingCostDAO {

    // average salary per hour (SEK/hour) per your choice
    private static final double AVG_SALARY_PER_HOUR = 350.0;

    // returns TeachingCost for instance
    public TeachingCost getCostForInstance(int instanceId) {
        String sqlPlanned = "SELECT COALESCE(SUM(pa.planned_hours * at.factor),0) AS planned FROM PlannedActivity pa JOIN ActivityType at ON pa.activity_id = at.activity_id WHERE pa.instance_id = ?";
        String sqlActual = "SELECT COALESCE(SUM(a.allocated_hours * at.factor),0) AS actual FROM Allocation a JOIN ActivityType at ON a.activity_id = at.activity_id WHERE a.instance_id = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps1 = c.prepareStatement(sqlPlanned);
                PreparedStatement ps2 = c.prepareStatement(sqlActual)) {

            ps1.setInt(1, instanceId);
            ps2.setInt(1, instanceId);
            double planned = 0.0, actual = 0.0;
            try (ResultSet r1 = ps1.executeQuery()) {
                if (r1.next())
                    planned = r1.getDouble("planned");
            }
            try (ResultSet r2 = ps2.executeQuery()) {
                if (r2.next())
                    actual = r2.getDouble("actual");
            }

            double plannedKsek = (planned * AVG_SALARY_PER_HOUR) / 1000.0;
            double actualKsek = (actual * AVG_SALARY_PER_HOUR) / 1000.0;

            // get basic instance info
            String infoSql = "SELECT cl.course_code, ci.period, ci.year FROM CourseInstance ci JOIN CourseLayout cl ON ci.layout_id = cl.layout_id WHERE ci.instance_id = ?";
            try (PreparedStatement ps3 = c.prepareStatement(infoSql)) {
                ps3.setInt(1, instanceId);
                try (ResultSet r3 = ps3.executeQuery()) {
                    if (r3.next()) {
                        String courseCode = r3.getString(1);
                        String period = r3.getString(2);
                        int year = r3.getInt(3);
                        return new TeachingCost(instanceId, courseCode, period, year, planned, actual, plannedKsek,
                                actualKsek);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("TeachingCost error: " + e.getMessage());
        }
        return null;
    }
}
