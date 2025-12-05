package model;

public class Teacher {
    private int id;
    private String firstName;
    private String lastName;
    private String designation;

    public Teacher(int id, String firstName, String lastName, String designation) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.designation = designation;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getDesignation() {
        return designation;
    }
}
