package model;

public class CourseInstance {
    public int instanceId;
    public String courseCode;
    public String courseName;
    public String period;
    public int year;
    public int numStudents;

    public CourseInstance(int instanceId, String courseCode, String courseName,
            String period, int year, int numStudents) {
        this.instanceId = instanceId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.period = period;
        this.year = year;
        this.numStudents = numStudents;
    }
}
