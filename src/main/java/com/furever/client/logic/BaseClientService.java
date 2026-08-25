package com.furever.client.logic;

import com.furever.client.communication.HttpClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for all client service classes
 * Provides common functionality for API communication including HttpClient management
 * Reduces code duplication and ensures consistent error handling across services
 */
public abstract class BaseClientService {
    protected HttpClient httpClient;
    
    /**
     * Initialize the base service with a new HttpClient instance
     * Subclasses can override to customize HttpClient configuration
     */
    public BaseClientService() {
        this.httpClient = new HttpClient();
    }
    
    /**
     * Convert array to List for consistent API return types
     * @param array Array to convert
     * @param <T> Type of array elements
     * @return List containing array elements
     */
    protected <T> List<T> arrayToList(T[] array) {
        return Arrays.asList(array);
    }
    
    /**
     * Check if a result is not null for boolean-style operations
     * @param result Result to check
     * @return true if result is not null, false otherwise
     */
    protected boolean isSuccessful(Object result) {
        return result != null;
    }
    
    /**
     * Get the HTTP client instance
     * @return HttpClient instance used by this service
     */
    protected HttpClient getHttpClient() {
        return httpClient;
    }
}
