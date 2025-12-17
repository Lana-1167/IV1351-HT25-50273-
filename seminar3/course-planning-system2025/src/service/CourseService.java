package service;

import dao.ActivityDAO;
import dao.AllocationDAO;
import dao.CourseDAO;
import dao.TeachingCostDAO;
import model.CourseInstance;
import model.ExerciseAllocationInfo;
import model.TeachingCost;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class CourseService {

    private final CourseDAO courseDAO = new CourseDAO();
    private final TeachingCostDAO costDAO = new TeachingCostDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    // ================= READ =================

    public List<CourseInstance> getAllInstances() {
        return courseDAO.getAllInstances();
    }

    public TeachingCost getTeachingCost(int instanceId) {
        return costDAO.getCostForInstance(instanceId);
    }

    public List<ExerciseAllocationInfo> getExerciseAllocationsForTeacher(int empId) {
        return allocationDAO.getExerciseAllocationsForTeacher(empId);
    }

    // ================= WRITE =================

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

    public boolean allocateTeacherTransactional(int empId, int instanceId, int activityId, double hours) {
        try (Connection c = dao.DBConnection.getConnection()) {
            c.setAutoCommit(false);

            CourseInstance ci = courseDAO.lockAndGetCourseInstance(instanceId);
            if (ci == null) {
                c.rollback();
                return false;
            }

            String lockSql = "SELECT a.* FROM Allocation a " +
                    "JOIN CourseInstance ci2 ON a.instance_id = ci2.instance_id " +
                    "WHERE a.emp_id = ? AND ci2.year = ? AND ci2.period = ? FOR UPDATE";

            try (PreparedStatement ps = c.prepareStatement(lockSql)) {
                ps.setInt(1, empId);
                ps.setInt(2, ci.year);
                ps.setString(3, ci.period);
                ps.executeQuery();
            }

            int count = allocationDAO.countDistinctInstancesForTeacherInPeriod(empId, ci.year, ci.period);

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
                throw new RuntimeException("Teacher exceeds 4 course instances in period");
            }

            allocationDAO.insertAllocation(c, empId, instanceId, activityId, hours);
            c.commit();
            return true;

        } catch (RuntimeException e) {
            // business mistake
            System.out.println("Х Allocation not allowed: " + e.getMessage());
            return false;

        } catch (Exception e) {
            // technical error
            System.out.println("Х Allocation failed due to system error.");
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

    public boolean addExerciseAndAllocate(
            int instanceId,
            int empId,
            double plannedHours,
            double allocatedHours) {

        try (Connection c = dao.DBConnection.getConnection()) {
            c.setAutoCommit(false);

            // 1 lock course instance
            CourseInstance ci = courseDAO.lockAndGetCourseInstance(instanceId);
            if (ci == null) {
                c.rollback();
                return false;
            }

            // 2 lock teacher allocations for this period
            String lockSql = "SELECT a.* FROM Allocation a " +
                    "JOIN CourseInstance ci2 ON a.instance_id = ci2.instance_id " +
                    "WHERE a.emp_id = ? AND ci2.year = ? AND ci2.period = ? FOR UPDATE";

            try (PreparedStatement ps = c.prepareStatement(lockSql)) {
                ps.setInt(1, empId);
                ps.setInt(2, ci.year);
                ps.setString(3, ci.period);
                ps.executeQuery();
            }

            // 3 count distinct course instances
            int count = allocationDAO.countDistinctInstancesForTeacherInPeriod(
                    empId, ci.year, ci.period);

            boolean alreadyAssigned = allocationDAO.hasAnyAllocationForInstance(c, empId, instanceId);

            if (count >= 4 && !alreadyAssigned) {
                c.rollback();
                throw new RuntimeException(
                        "Teacher already allocated to 4 course instances in this period");
            }

            // 4 ensure Exercise activity exists
            int activityId = activityDAO.ensureActivity(c, "Exercise", 1.5);

            // 5 planned hours
            activityDAO.upsertPlannedActivity(
                    c, instanceId, activityId, plannedHours);

            // 6 allocation
            allocationDAO.insertAllocation(
                    c, empId, instanceId, activityId, allocatedHours);

            c.commit();
            return true;

        } catch (RuntimeException e) {
            // business mistake
            System.out.println("Х Allocation not allowed: " + e.getMessage());
            return false;

        } catch (Exception e) {
            // technical error
            System.out.println("Х Allocation failed due to system error.");
            return false;
        }
    }

}
