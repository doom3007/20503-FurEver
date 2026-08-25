package com.furever.server.logic;

import com.furever.common.models.Category;
import com.furever.server.data.CategoryDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for category management
 * Provides business logic for category operations including validation
 * Handles category CRUD operations with data validation
 */
public class CategoryService extends BaseService {
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
        validateNotNullOrEmpty(category.getCategoryName(), "שם קטגוריה");
        return categoryDAO.addCategory(category);
    }
    
    public boolean updateCategory(Category category) throws SQLException {
        validatePositiveId(category.getCategoryID(), "מזהה קטגוריה");
        return categoryDAO.updateCategory(category);
    }
    
    public boolean deleteCategory(int categoryID) throws SQLException {
        return categoryDAO.deleteCategory(categoryID);
    }
}
