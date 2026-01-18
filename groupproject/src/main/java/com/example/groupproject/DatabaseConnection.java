package com.example.groupproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // UPM Oracle Settings
    private static final String DB_URL = "jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:fsktm";
    private static final String DB_USER = "B229259";  // Your Metric Number
    private static final String DB_PASSWORD = "229259";  // Your Password

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Oracle JDBC Driver not found");
        }
    }
}
