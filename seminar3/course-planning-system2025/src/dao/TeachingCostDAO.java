package dao;

import model.TeachingCost;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TeachingCostDAO {

    // Average salary per hour (used ONLY for planned cost)
    private static final double AVG_SALARY_PER_HOUR = 350.0;

    public TeachingCost getCostForInstance(int instanceId) {

        // ===== PLANNED HOURS =====
        String sqlPlanned = "SELECT planned_sum " +
                "     + (num_students * 0.05) " +
                "     + CEIL(num_students / 30.0) * 5 AS planned_hours " +
                "FROM ( " +
                "   SELECT ci.num_students, " +
                "          COALESCE(SUM(pa.planned_hours * at.factor), 0) AS planned_sum " +
                "   FROM CourseInstance ci " +
                "   LEFT JOIN PlannedActivity pa ON ci.instance_id = pa.instance_id " +
                "   LEFT JOIN ActivityType at ON pa.activity_id = at.activity_id " +
                "   WHERE ci.instance_id = ? " +
                "   GROUP BY ci.num_students " +
                ") t";

        // ===== ACTUAL HOURS =====
        String sqlActual = "SELECT COALESCE(SUM(a.allocated_hours * at.factor), 0) AS actual_hours " +
                "FROM Allocation a " +
                "JOIN ActivityType at ON a.activity_id = at.activity_id " +
                "WHERE a.instance_id = ?";

        try (Connection c = DBConnection.getConnection();
                PreparedStatement psPlanned = c.prepareStatement(sqlPlanned);
                PreparedStatement psActual = c.prepareStatement(sqlActual)) {

            psPlanned.setInt(1, instanceId);
            psActual.setInt(1, instanceId);

            double plannedHours = 0;
            double actualHours = 0;

            // ---- read planned ----
            try (ResultSet rs = psPlanned.executeQuery()) {
                if (rs.next()) {
                    plannedHours = rs.getDouble("planned_hours");
                }
            }

            // ---- read actual ----
            try (ResultSet rs = psActual.executeQuery()) {
                if (rs.next()) {
                    actualHours = rs.getDouble("actual_hours");
                }
            }

            // ---- cost ----
            double plannedKsek = (plannedHours * AVG_SALARY_PER_HOUR) / 1000.0;
            double actualKsek = computeActualCostFromSalary(c, instanceId);

            // ---- basic instance info ----
            String infoSql = "SELECT cl.course_code, ci.period, ci.year " +
                    "FROM CourseInstance ci " +
                    "JOIN CourseLayout cl ON ci.layout_id = cl.layout_id " +
                    "WHERE ci.instance_id = ?";

            try (PreparedStatement psInfo = c.prepareStatement(infoSql)) {
                psInfo.setInt(1, instanceId);
                try (ResultSet rs = psInfo.executeQuery()) {
                    if (rs.next()) {
                        return new TeachingCost(
                                instanceId,
                                rs.getString("course_code"),
                                rs.getString("period"),
                                rs.getInt("year"),
                                plannedHours,
                                actualHours,
                                plannedKsek,
                                actualKsek);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("TeachingCost error: " + e.getMessage());
        }

        return null;
    }

    // ===== ACTUAL COST BASED ON REAL SALARY =====
    private double computeActualCostFromSalary(Connection c, int instanceId) throws Exception {

        String sql = "SELECT COALESCE(SUM(a.allocated_hours * at.factor * e.salary / 160.0), 0) AS cost " +
                "FROM Allocation a " +
                "JOIN ActivityType at ON a.activity_id = at.activity_id " +
                "JOIN Employee e ON a.emp_id = e.emp_id " +
                "WHERE a.instance_id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, instanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("cost") / 1000.0; // → KSEK
                }
            }
        }
        return 0.0;
    }
}
