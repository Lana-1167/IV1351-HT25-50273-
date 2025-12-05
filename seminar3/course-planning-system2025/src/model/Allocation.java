package model;

public class Allocation {
    public String instanceId;
    public int teacherId;
    public String activity;
    public int hours;

    public Allocation(String instanceId, int teacherId, String activity, int hours) {
        this.instanceId = instanceId;
        this.teacherId = teacherId;
        this.activity = activity;
        this.hours = hours;
    }
}
