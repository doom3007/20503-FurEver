package com.furever.client.logic;

import com.furever.common.models.Category;

import java.io.IOException;
import java.util.List;

/**
 * Service class for category-related API communication
 * Handles category data retrieval for pet categorization
 * Manages communication with category endpoints
 */
public class CategoryClientService extends BaseClientService {
    
    public List<Category> getAllCategories() throws IOException {
        Category[] categories = httpClient.get("/categories", Category[].class);
        return arrayToList(categories);
    }
}
