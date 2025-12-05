package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import dao.*;
import model.CourseInstance;

public class CourseService {

    private final CourseDAO courseDAO = new CourseDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    // compute and print costs using TeachingCostDAO in MainCLI
    // Increase students with transaction and SELECT FOR UPDATE
    public boolean increaseStudentsTransactional(int instanceId, int delta) {
        try (Connection c = dao.DBConnection.getConnection()) {
            c.setAutoCommit(false);
            CourseInstance ci = courseDAO.lockAndGetCourseInstance(instanceId);
            if (ci == null) {
                c.rollback();
                return false;
            }
            courseDAO.increaseStudents(instanceId, delta);
            c.commit();
            return true;
        } catch (Exception e) {
            System.out.println("increaseStudents error: " + e.getMessage());
            return false;
        }
    }

    // allocate with limit check: ensures no more than 4 distinct instances in same
    // period/year for the teacher
    public boolean allocateTeacherTransactional(int empId, int instanceId, int activityId, double hours) {
        try (Connection c = dao.DBConnection.getConnection()) {
            c.setAutoCommit(false);

            // lock the target courseinstance row to get period/year
            CourseInstance ci = courseDAO.lockAndGetCourseInstance(instanceId);
            if (ci == null) {
                c.rollback();
                return false;
            }

            // lock allocation rows for this teacher in that year/period to avoid races
            String lockSql = "SELECT a.* FROM Allocation a JOIN CourseInstance ci2 ON a.instance_id = ci2.instance_id WHERE a.emp_id = ? AND ci2.year = ? AND ci2.period = ? FOR UPDATE";
            try (PreparedStatement psLock = c.prepareStatement(lockSql)) {
                psLock.setInt(1, empId);
                psLock.setInt(2, ci.year);
                psLock.setString(3, ci.period);
                psLock.executeQuery(); // lock
            }

            int count = allocationDAO.countDistinctInstancesForTeacherInPeriod(empId, ci.year, ci.period);
            // check if already assigned to same instance (different activity still counts
            // as same instance)
            String existsSql = "SELECT 1 FROM Allocation WHERE emp_id = ? AND instance_id = ? AND activity_id = ?";
            boolean alreadyAssigned = false;
            try (PreparedStatement ps = c.prepareStatement(existsSql)) {
                ps.setInt(1, empId);
                ps.setInt(2, instanceId);
                ps.setInt(3, activityId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        alreadyAssigned = true;
                }
            }

            int futureCount = count + (alreadyAssigned ? 0 : 1);
            if (futureCount > 4) {
                c.rollback();
                throw new SQLException(
                        "Allocation would exceed 4 distinct course instances for this teacher in period " + ci.period);
            }

            allocationDAO.insertAllocation(c, empId, instanceId, activityId, hours);

            c.commit();
            return true;
        } catch (Exception e) {
            System.out.println("Allocation failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deallocateTeacher(int empId, int instanceId, int activityId) {
        try {
            return allocationDAO.deleteAllocation(empId, instanceId, activityId);
        } catch (Exception e) {
            System.out.println("Deallocate error: " + e.getMessage());
            return false;
        }
    }

    // Add Exercise to instance and allocate teacher
    public boolean addExerciseAndAllocate(int instanceId, int empId, double plannedHours, double allocHours) {
        try (Connection c = dao.DBConnection.getConnection()) {
            c.setAutoCommit(false);
            int activityId = activityDAO.ensureActivity(c, "Exercise", 1.5);
            activityDAO.upsertPlannedActivity(c, instanceId, activityId, plannedHours);
            allocationDAO.insertAllocation(c, empId, instanceId, activityId, allocHours);
            c.commit();
            return true;
        } catch (Exception e) {
            System.out.println("addExercise error: " + e.getMessage());
            return false;
        }
    }
}
