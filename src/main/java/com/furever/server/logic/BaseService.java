package com.furever.server.logic;

import java.sql.SQLException;

/**
 * Base class for all service classes
 * Provides common validation methods and error handling patterns
 * Reduces code duplication and ensures consistent validation across services
 */
public abstract class BaseService {
    
    /**
     * Validate that a string is not null or empty
     * @param value String to validate
     * @param fieldName Name of the field for error message
     * @throws IllegalArgumentException if validation fails
     */
    protected void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " לא יכול להיות ריק");
        }
    }
    
    /**
     * Validate that an ID is positive
     * @param id ID to validate
     * @param fieldName Name of the field for error message
     * @throws IllegalArgumentException if validation fails
     */
    protected void validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " לא תקין");
        }
    }
    
    /**
     * Validate that a number is not negative
     * @param value Number to validate
     * @param fieldName Name of the field for error message
     * @throws IllegalArgumentException if validation fails
     */
    protected void validateNotNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " לא יכול להיות שלילי");
        }
    }
    
    /**
     * Validate that a status value is among allowed values
     * @param status Status to validate
     * @param allowedValues Array of allowed status values
     * @param fieldName Name of the field for error message
     * @throws IllegalArgumentException if validation fails
     */
    protected void validateStatus(String status, String[] allowedValues, String fieldName) {
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " לא יכול להיות ריק");
        }
        
        for (String allowedValue : allowedValues) {
            if (status.equals(allowedValue)) {
                return;
            }
        }
        
        throw new IllegalArgumentException(fieldName + " לא תקין");
    }
    
    /**
     * Validate that a status value is among allowed values (SQLException version)
     * @param status Status to validate
     * @param allowedValues Array of allowed status values
     * @param fieldName Name of the field for error message
     * @throws SQLException if validation fails
     */
    protected void validateStatusSql(String status, String[] allowedValues, String fieldName) throws SQLException {
        if (status == null || status.isEmpty()) {
            throw new SQLException(fieldName + " לא יכול להיות ריק");
        }
        
        for (String allowedValue : allowedValues) {
            if (status.equals(allowedValue)) {
                return;
            }
        }
        
        throw new SQLException(fieldName + " לא תקין");
    }
    
    /**
     * Validate that a string is not null or empty (SQLException version)
     * @param value String to validate
     * @param fieldName Name of the field for error message
     * @throws SQLException if validation fails
     */
    protected void validateNotNullOrEmptySql(String value, String fieldName) throws SQLException {
        if (value == null || value.isEmpty()) {
            throw new SQLException(fieldName + " לא יכול להיות ריק");
        }
    }
    
    /**
     * Validate that an ID is positive (SQLException version)
     * @param id ID to validate
     * @param fieldName Name of the field for error message
     * @throws SQLException if validation fails
     */
    protected void validatePositiveIdSql(int id, String fieldName) throws SQLException {
        if (id <= 0) {
            throw new SQLException(fieldName + " לא תקין");
        }
    }
}
