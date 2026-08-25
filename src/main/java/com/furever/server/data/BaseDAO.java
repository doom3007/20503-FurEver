package com.furever.server.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Base class for all Data Access Objects
 * Provides common database operation patterns and error handling
 * Reduces code duplication and ensures consistent resource management
 */
public abstract class BaseDAO {
    
    /**
     * Execute a SELECT query and return the ResultSet
     * Handles connection management and statement preparation
     * @param query SQL query with placeholders
     * @param params Parameters to substitute in placeholders
     * @return ResultSet containing query results
     * @throws SQLException if database error occurs
     */
    protected ResultSet executeQuery(String query, Object... params) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query);
        
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        
        return pstmt.executeQuery();
    }
    
    /**
     * Execute an INSERT/UPDATE/DELETE query
     * Handles connection management and statement preparation
     * @param query SQL query with placeholders
     * @param params Parameters to substitute in placeholders
     * @return Number of affected rows
     * @throws SQLException if database error occurs
     */
    protected int executeUpdate(String query, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            return pstmt.executeUpdate();
        }
    }
    

    
    /**
     * Check if a record exists matching given criteria
     * Common pattern for duplicate checking operations
     * @param tableName Name of the table to check
     * @param fieldName Name of the field to check
     * @param value Value to check for existence
     * @return true if record exists, false otherwise
     * @throws SQLException if database error occurs
     */
    protected boolean fieldExists(String tableName, String fieldName, String value) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + fieldName + " = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Safely extract integer from ResultSet column
     * Handles potential null values gracefully
     * @param rs ResultSet to extract from
     * @param columnName Name of the column to extract
     @return Integer value or null if column is null
     * @throws SQLException if database error occurs
     */
    protected Integer getIntegerSafely(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
    
    /**
     * Safely extract LocalDate from ResultSet column
     * Handles potential null values gracefully
     * @param rs ResultSet to extract from
     * @param columnName Name of the column to extract
     * @return LocalDate value or null if column is null
     * @throws SQLException if database error occurs
     */
    protected java.time.LocalDate getLocalDateSafely(ResultSet rs, String columnName) throws SQLException {
        java.sql.Date sqlDate = rs.getDate(columnName);
        return sqlDate == null ? null : sqlDate.toLocalDate();
    }
}
