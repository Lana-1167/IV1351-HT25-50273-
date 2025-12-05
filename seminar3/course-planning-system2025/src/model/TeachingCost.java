package model;

public class TeachingCost {
    public int instanceId;
    public String courseCode;
    public String period;
    public int year;
    public double plannedHours;
    public double actualHours;
    public double plannedKsek;
    public double actualKsek;

    public TeachingCost(int instanceId, String courseCode, String period, int year,
            double plannedHours, double actualHours, double plannedKsek, double actualKsek) {
        this.instanceId = instanceId;
        this.courseCode = courseCode;
        this.period = period;
        this.year = year;
        this.plannedHours = plannedHours;
        this.actualHours = actualHours;
        this.plannedKsek = plannedKsek;
        this.actualKsek = actualKsek;
    }
}
