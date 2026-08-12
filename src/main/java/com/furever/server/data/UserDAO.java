package com.furever.server.data;

import com.furever.common.models.User;
import com.furever.server.util.PasswordUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public User authenticateUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM User WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                // Verify password hash
                if (PasswordUtil.verifyPassword(password, user.getPassword())) {
                    // Don't return the hashed password in the user object
                    user.setPassword(null);
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }
    
    public User getUserById(int userID) throws SQLException {
        String query = "SELECT * FROM User WHERE userID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        }
        return null;
    }
    
    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM User WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        }
        return null;
    }
    
    public User getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM User WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        }
        return null;
    }
    
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM User ORDER BY registrationDate DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        }
        return users;
    }
    
    public boolean addUser(User user) throws SQLException {
        // Check for duplicates using existing methods
        if (usernameExists(user.getUsername()) || emailExists(user.getEmail()) || phoneExists(user.getPhone())) {
            return false;
        }

        user.setRegistrationDate(LocalDate.now());

        // Hash the password before storing
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());

        String query = "INSERT INTO User (username, password, fullName, email, phone, isAdmin, registrationDate) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setBoolean(6, user.isAdmin());
            pstmt.setDate(7, Date.valueOf(user.getRegistrationDate()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setUserID(generatedKeys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }
    
    public boolean updateUser(User user) throws SQLException {
        String query = "UPDATE User SET fullName = ?, email = ?, phone = ? WHERE userID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setInt(4, user.getUserID());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateUserPassword(int userID, String newPassword) throws SQLException {
        // Hash the new password before storing
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        
        String query = "UPDATE User SET password = ? WHERE userID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userID);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteUser(int userID) throws SQLException {
        String query = "DELETE FROM User WHERE userID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userID);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean usernameExists(String username) throws SQLException {
        return fieldExists("username", username);
    }

    public boolean emailExists(String email) throws SQLException {
        return fieldExists("email", email);
    }

    public boolean phoneExists(String phone) throws SQLException {
        return fieldExists("phone", phone);
    }

    private boolean fieldExists(String fieldName, String value) throws SQLException {
        String query = "SELECT COUNT(*) FROM User WHERE " + fieldName + " = ?";

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
    
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserID(rs.getInt("userID"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("fullName"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setAdmin(rs.getBoolean("isAdmin"));
        user.setRegistrationDate(rs.getDate("registrationDate").toLocalDate());
        return user;
    }
}
