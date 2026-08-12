package com.furever.common.models;

import java.time.LocalDate;

public class User {
    private int userID;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private boolean isAdmin;
    private LocalDate registrationDate;
    
    public User() {
    }
    
    public User(String username, String password, String fullName, 
                String email, String phone, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.isAdmin = isAdmin;
        this.registrationDate = LocalDate.now();
    }
    
    public int getUserID() {
        return userID;
    }
    
    public void setUserID(int userID) {
        this.userID = userID;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
    
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    @Override
    public String toString() {
        return fullName + " (" + username + ") - Admin: " + isAdmin;
    }
}
