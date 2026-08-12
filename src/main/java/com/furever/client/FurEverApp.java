package com.furever.client;

import com.furever.client.ui.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FurEverApp extends Application {
    
    private static Stage primaryStage;
    private static com.furever.common.models.User currentUser;
    private static String authToken;
    private static FurEverApp instance;
    
    @Override
    public void start(Stage stage) throws IOException {
        instance = this;
        primaryStage = stage;
        primaryStage.setTitle("FurEver - מערכת אימוץ חיות מחמד");
        
        showLoginScreen();
        
        primaryStage.show();
    }
    
    public static void showLoginScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(FurEverApp.class.getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setMainApp(instance);
        
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
    }
    
    public static void showUserScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(FurEverApp.class.getResource("/fxml/user-dashboard.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
    }
    
    public static void showAdminScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(FurEverApp.class.getResource("/fxml/admin-dashboard.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
    }
    
    public static void setCurrentUser(com.furever.common.models.User user) {
        currentUser = user;
    }
    
    public static com.furever.common.models.User getCurrentUser() {
        return currentUser;
    }
    
    public static void setAuthToken(String token) {
        authToken = token;
    }
    
    public static String getAuthToken() {
        return authToken;
    }
    
    public static void clearAuth() {
        currentUser = null;
        authToken = null;
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
