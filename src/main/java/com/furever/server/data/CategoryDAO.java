package com.furever.server.data;

import com.furever.common.models.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM Category";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Category category = new Category();
                category.setCategoryID(rs.getInt("categoryID"));
                category.setCategoryName(rs.getString("categoryName"));
                categories.add(category);
            }
        }
        return categories;
    }
    
    public Category getCategoryById(int categoryID) throws SQLException {
        String query = "SELECT * FROM Category WHERE categoryID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, categoryID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Category category = new Category();
                category.setCategoryID(rs.getInt("categoryID"));
                category.setCategoryName(rs.getString("categoryName"));
                return category;
            }
        }
        return null;
    }
    
    public Category getCategoryByName(String categoryName) throws SQLException {
        String query = "SELECT * FROM Category WHERE categoryName = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Category category = new Category();
                category.setCategoryID(rs.getInt("categoryID"));
                category.setCategoryName(rs.getString("categoryName"));
                return category;
            }
        }
        return null;
    }
    
    public boolean addCategory(Category category) throws SQLException {
        String query = "INSERT INTO Category (categoryName) VALUES (?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, category.getCategoryName());
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    category.setCategoryID(generatedKeys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }
    
    public boolean updateCategory(Category category) throws SQLException {
        String query = "UPDATE Category SET categoryName = ? WHERE categoryID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, category.getCategoryName());
            pstmt.setInt(2, category.getCategoryID());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteCategory(int categoryID) throws SQLException {
        String query = "DELETE FROM Category WHERE categoryID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, categoryID);
            return pstmt.executeUpdate() > 0;
        }
    }
}
