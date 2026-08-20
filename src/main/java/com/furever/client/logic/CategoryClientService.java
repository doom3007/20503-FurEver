package com.furever.client.logic;

import com.furever.client.communication.HttpClient;
import com.furever.common.models.Category;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Service class for category-related API communication
 * Handles category data retrieval for pet categorization
 * Manages communication with category endpoints
 */
public class CategoryClientService {
    private HttpClient httpClient;
    
    public CategoryClientService() {
        this.httpClient = new HttpClient();
    }
    
    public List<Category> getAllCategories() throws IOException {
        Category[] categories = httpClient.get("/categories", Category[].class);
        return Arrays.asList(categories);
    }
}
