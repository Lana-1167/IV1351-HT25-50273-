package dao;

import java.sql.Connection;
//import java.sql.DriverManager;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/teaching2025";
    private static final String USER = "postgres";
    private static final String PASSWORD = "lana_lana67";

    public static Connection getConnection() throws java.sql.SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Postgres driver not found: " + e.getMessage());
        }
        return java.sql.DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
