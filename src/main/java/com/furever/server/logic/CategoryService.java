package com.furever.server.logic;

import com.furever.common.models.Category;
import com.furever.server.data.CategoryDAO;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {
    private CategoryDAO categoryDAO;
    
    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }
    
    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAllCategories();
    }
    
    public Category getCategoryById(int categoryID) throws SQLException {
        return categoryDAO.getCategoryById(categoryID);
    }
    
    public Category getCategoryByName(String categoryName) throws SQLException {
        return categoryDAO.getCategoryByName(categoryName);
    }
    
    public boolean addCategory(Category category) throws SQLException {
        if (category.getCategoryName() == null || category.getCategoryName().isEmpty()) {
            throw new IllegalArgumentException("שם קטגוריה לא יכול להיות ריק");
        }
        
        return categoryDAO.addCategory(category);
    }
    
    public boolean updateCategory(Category category) throws SQLException {
        if (category.getCategoryID() <= 0) {
            throw new IllegalArgumentException("מזהה קטגוריה לא תקין");
        }
        
        return categoryDAO.updateCategory(category);
    }
    
    public boolean deleteCategory(int categoryID) throws SQLException {
        return categoryDAO.deleteCategory(categoryID);
    }
}
