package com.furever.client.logic;

import com.furever.client.FurEverApp;
import com.furever.client.communication.HttpClient;
import com.furever.common.models.User;
import com.furever.common.util.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for user-related API communication
 * Handles user authentication, registration, and user data retrieval
 * Manages JWT token storage and communication with the user endpoints
 */
public class UserClientService {
    private HttpClient httpClient;
    private Gson gson;
    
    public UserClientService() {
        this.httpClient = new HttpClient();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();
    }
    
    public User login(String username, String password) throws IOException {
        LoginRequest loginRequest = new LoginRequest(username, password);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = httpClient.post("/auth", loginRequest, Map.class);
        
        if (response != null && response.containsKey("user") && response.containsKey("token")) {
            User user = gson.fromJson(gson.toJson(response.get("user")), User.class);
            String token = (String) response.get("token");
            FurEverApp.setAuthToken(token);
            return user;
        }
        return null;
    }
    
    public User register(User user) throws IOException {
        return httpClient.post("/users", user, User.class);
    }
    
    public User registerWithAdminCode(User user, String adminCode) throws IOException {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("username", user.getUsername());
        requestData.put("password", user.getPassword());
        requestData.put("fullName", user.getFullName());
        requestData.put("email", user.getEmail());
        requestData.put("phone", user.getPhone());
        requestData.put("adminCode", adminCode);
        
        return httpClient.post("/users", requestData, User.class);
    }
    
    public List<User> getAllUsers() throws IOException {
        User[] users = httpClient.get("/users", User[].class);
        return Arrays.asList(users);
    }
    
    private static class LoginRequest {
        String username;
        String password;
        
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
