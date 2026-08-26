package com.furever.client.ui;

import com.furever.client.FurEverApp;
import com.furever.client.logic.PetClientService;
import com.furever.common.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Base class for dashboard controllers
 * Provides common functionality for both user and admin dashboards
 * Reduces code duplication and ensures consistent behavior across dashboards
 */
public abstract class BaseDashboardController {
    
    @FXML
    protected Label welcomeLabel;
    
    @FXML
    protected TextArea statusLabel;
    
    protected PetClientService petClientService;
    protected Timer sessionCheckTimer;
    
    /**
     * Initialize common dashboard components
     * Subclasses should call this in their initialize method
     */
    protected void initializeBase() {
        this.petClientService = new PetClientService();
        startSessionChecker();
        
        User currentUser = FurEverApp.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("שלום, " + currentUser.getFullName());
        }
    }
    
    /**
     * Start a background timer to check session validity every 30 seconds
     * Automatically redirects to login screen if session expires
     */
    protected void startSessionChecker() {
        sessionCheckTimer = UIUtils.createSessionChecker(() -> {
            try {
                petClientService.getAllPets();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Handle logout - clear authentication and redirect to login
     * Can be overridden by subclasses for custom logout behavior
     */
    @FXML
    protected void handleLogout() {
        if (sessionCheckTimer != null) {
            sessionCheckTimer.cancel();
        }
        FurEverApp.clearAuth();
        try {
            FurEverApp.showLoginScreen();
        } catch (IOException e) {
            UIUtils.showError(statusLabel, "שגיאה בהתנתקות: " + e.getMessage());
        }
    }
    
    /**
     * Common pattern for loading data with error handling
     * @param loader Function that loads the data
     * @param successMessage Message to show on success
     * @param errorMessagePrefix Prefix for error messages
     * @param label Status label to display messages (uses default if null)
     */
    protected void loadDataWithHandling(DataLoader loader, String successMessage, String errorMessagePrefix, TextArea label) {
        TextArea targetLabel = (label != null) ? label : statusLabel;
        targetLabel.clear();
        try {
            int count = loader.load();
            UIUtils.showInfo(targetLabel, successMessage + count);
        } catch (IOException e) {
            UIUtils.showError(targetLabel, errorMessagePrefix + e.getMessage());
        }
    }
    
    /**
     * Common pattern for loading data with error handling (uses default status label)
     * @param loader Function that loads the data
     * @param successMessage Message to show on success
     * @param errorMessagePrefix Prefix for error messages
     */
    protected void loadDataWithHandling(DataLoader loader, String successMessage, String errorMessagePrefix) {
        loadDataWithHandling(loader, successMessage, errorMessagePrefix, null);
    }
    
    /**
     * Functional interface for data loading operations
     */
    @FunctionalInterface
    protected interface DataLoader {
        int load() throws IOException;
    }
    
    /**
     * Clean up resources when controller is destroyed
     */
    public void cleanup() {
        if (sessionCheckTimer != null) {
            sessionCheckTimer.cancel();
        }
    }
}
