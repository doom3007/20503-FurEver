package com.furever.client.ui;

import com.furever.client.FurEverApp;
import com.furever.client.logic.UserClientService;
import com.furever.common.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the login screen
 * Handles user authentication and navigation to registration
 * Validates user input and communicates with the authentication service
 */
public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private TextArea messageLabel;
    
    private UserClientService userClientService;
    private FurEverApp mainApp;
    
    public LoginController() {
        this.userClientService = new UserClientService();
    }
    
    @FXML
    public void initialize() {
        usernameField.setOnKeyPressed(this::handleKeyPress);
        passwordField.setOnKeyPressed(this::handleKeyPress);
    }
    
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }
    
    public void setMainApp(FurEverApp mainApp) {
        this.mainApp = mainApp;
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("אנא מלא את כל השדות");
            return;
        }
        
        try {
            User user = userClientService.login(username, password);
            if (user != null) {
                FurEverApp.setCurrentUser(user);
                messageLabel.setText("");
                
                if (user.isAdmin()) {
                    FurEverApp.showAdminScreen();
                } else {
                    FurEverApp.showUserScreen();
                }
            } else {
                messageLabel.setText("שם משתמש או סיסמה שגויים");
                UIUtils.blinkError(messageLabel);
            }
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            messageLabel.setText(errorMessage);
            UIUtils.blinkError(messageLabel);
        }
    }
    
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            RegisterController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("הרשמה");
            stage.setScene(new Scene(root, 400, 450));
            stage.showAndWait();
            
            if (controller.isRegistrationSuccessful()) {
                usernameField.setText(controller.getUsername());
                passwordField.setText(controller.getPassword());
                handleLogin();
            }
        } catch (IOException e) {
            messageLabel.setText("שגיאה בפתיחת חלון הרשמה: " + e.getMessage());
            UIUtils.blinkError(messageLabel);
        }
    }
}
