package com.furever.server.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/furever";
    private static final String USERNAME = "root"; // Put your MySQL username here
    private static final String PASSWORD = "root"; // Put your MySQL password here
    
    private static Connection connection = null;
    
    /**
     * Get a database connection, creating one if needed
     * Uses singleton pattern to maintain a single connection instance
     * @return Active database connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Properties props = new Properties();
                props.setProperty("user", USERNAME);
                props.setProperty("password", PASSWORD);
                props.setProperty("useSSL", "false");
                props.setProperty("serverTimezone", "UTC");
                props.setProperty("characterEncoding", "UTF-8");
                props.setProperty("useUnicode", "true");
                
                connection = DriverManager.getConnection(URL, props);
            } catch (SQLException e) {
                throw new SQLException("נכשל בהתחברות למסד נתונים: " + e.getMessage(), e);
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("שגיאה בסגירת החיבור למסד נתונים: " + e.getMessage());
            }
        }
    }
}
