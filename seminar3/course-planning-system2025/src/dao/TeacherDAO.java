package dao;

import model.Teacher;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO {

    public List<Teacher> getAllTeachers() {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT emp_id, first_name, last_name, designation FROM Employee ORDER BY emp_id";
        try (Connection c = DBConnection.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Teacher(
                        rs.getInt("emp_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("designation")));
            }
        } catch (Exception e) {
            System.out.println("Error loading teachers: " + e.getMessage());
        }
        return list;
    }
}
