package model;

public class ExerciseAllocationInfo {

    public int empId;
    public String teacherName;
    public String courseCode;
    public int instanceId;
    public int year;
    public String period;
    public double allocatedHours;

    public ExerciseAllocationInfo(
            int empId,
            String teacherName,
            String courseCode,
            int instanceId,
            int year,
            String period,
            double allocatedHours) {

        this.empId = empId;
        this.teacherName = teacherName;
        this.courseCode = courseCode;
        this.instanceId = instanceId;
        this.year = year;
        this.period = period;
        this.allocatedHours = allocatedHours;
    }
}
