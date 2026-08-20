package com.furever.server.logic;

import com.furever.common.models.User;
import com.furever.server.data.UserDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for user management
 * Provides business logic for managing user accounts including:
 * - User authentication (login)
 * - Registration of new users with duplicate checking
 * - Updating user details and passwords
 * - Deleting users
 * - Retrieving user information
 */
public class UserService {
    private UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    public User authenticateUser(String username, String password) throws SQLException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("שם משתמש לא יכול להיות ריק");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("סיסמה לא יכולה להיות ריקה");
        }
        
        return userDAO.authenticateUser(username, password);
    }
    
    public User getUserById(int userID) throws SQLException {
        return userDAO.getUserById(userID);
    }
    
    public User getUserByUsername(String username) throws SQLException {
        return userDAO.getUserByUsername(username);
    }
    
    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }
    
    public boolean registerUser(User user) throws SQLException {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("שם משתמש לא יכול להיות ריק");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("סיסמה לא יכולה להיות ריקה");
        }
        if (user.getFullName() == null || user.getFullName().isEmpty()) {
            throw new IllegalArgumentException("שם מלא לא יכול להיות ריק");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("אימייל לא יכול להיות ריק");
        }
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            throw new IllegalArgumentException("טלפון לא יכול להיות ריק");
        }
        
        if (userDAO.usernameExists(user.getUsername())) {
            throw new IllegalArgumentException("שם המשתמש כבר קיים");
        }
        if (userDAO.emailExists(user.getEmail())) {
            throw new IllegalArgumentException("כתובת האימייל כבר קיימת");
        }
        if (userDAO.phoneExists(user.getPhone())) {
            throw new IllegalArgumentException("מספר הטלפון כבר קיים");
        }
        
        return userDAO.addUser(user);
    }
    
    public boolean updateUser(User user) throws SQLException {
        if (user.getUserID() <= 0) {
            throw new IllegalArgumentException("מזהה משתמש לא תקין");
        }
        
        return userDAO.updateUser(user);
    }
    
    public boolean updateUserPassword(int userID, String newPassword) throws SQLException {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("סיסמה חדשה לא יכולה להיות ריקה");
        }
        
        return userDAO.updateUserPassword(userID, newPassword);
    }
    
    public boolean deleteUser(int userID) throws SQLException {
        return userDAO.deleteUser(userID);
    }
}
