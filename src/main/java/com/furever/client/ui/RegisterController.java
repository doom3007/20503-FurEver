package com.furever.client.ui;

import com.furever.client.logic.UserClientService;
import com.furever.common.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the registration screen
 * Handles new user registration with validation and admin code support
 * Validates email format, phone numbers, password matching, and duplicate prevention
 */
public class RegisterController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField adminCodeField;
    
    @FXML
    private Label messageLabel;
    
    private UserClientService userClientService;
    private boolean registrationSuccessful = false;
    
    @FXML
    public void initialize() {
        this.userClientService = new UserClientService();
        
        usernameField.setOnKeyPressed(this::handleEnterKey);
        passwordField.setOnKeyPressed(this::handleEnterKey);
        confirmPasswordField.setOnKeyPressed(this::handleEnterKey);
        fullNameField.setOnKeyPressed(this::handleEnterKey);
        emailField.setOnKeyPressed(this::handleEnterKey);
        phoneField.setOnKeyPressed(this::handleEnterKey);
        adminCodeField.setOnKeyPressed(this::handleEnterKey);
    }
    
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode().toString().equals("ENTER")) {
            handleRegister();
        }
    }
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String adminCode = adminCodeField.getText();
        
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || 
            email.isEmpty() || phone.isEmpty()) {
            UIUtils.showError(messageLabel, "אנא מלא את כל השדות הנדרשים");
            return;
        }
        
        if (!ValidationUtils.isValidPhone(phone)) {
            UIUtils.showError(messageLabel, ValidationUtils.getPhoneValidationError());
            return;
        }
        
        if (!ValidationUtils.isValidEmail(email)) {
            UIUtils.showError(messageLabel, ValidationUtils.getEmailValidationError());
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            UIUtils.showError(messageLabel, "הסיסמאות אינן תואמות");
            return;
        }
        
        if (password.length() < 6) {
            UIUtils.showError(messageLabel, "הסיסמה חייבת להכיל לפחות 6 תווים");
            return;
        }
        
        try {
            User user = new User(username, password, fullName, email, phone, false);
            User registeredUser;
            
            if (adminCode != null && !adminCode.isEmpty()) {
                registeredUser = userClientService.registerWithAdminCode(user, adminCode);
            } else {
                registeredUser = userClientService.register(user);
            }
            
            if (registeredUser != null) {
                registrationSuccessful = true;
                UIUtils.showSuccess(messageLabel, "הרשמה בוצעה בהצלחה!");
                
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.close();
                });
            } else {
                UIUtils.showError(messageLabel, "שם המשתמש כבר קיים במערכת");
            }
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("שם המשתמש כבר קיים")) {
                UIUtils.showError(messageLabel, "שם המשתמש כבר קיים במערכת");
            } else if (errorMessage.contains("כתובת האימייל כבר קיימת")) {
                UIUtils.showError(messageLabel, "כתובת האימייל כבר קיימת במערכת");
            } else if (errorMessage.contains("מספר הטלפון כבר קיים")) {
                UIUtils.showError(messageLabel, "מספר הטלפון כבר קיים במערכת");
            } else if (errorMessage.contains("לא יכול להיות ריק")) {
                UIUtils.showError(messageLabel, errorMessage);
            } else if (errorMessage.contains("קוד מנהל לא תקין")) {
                UIUtils.showError(messageLabel, "קוד מנהל לא תקין");
            } else {
                UIUtils.showError(messageLabel, "שגיאה בהרשמה: " + errorMessage);
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
    
    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }
    
    public String getUsername() {
        return usernameField.getText();
    }
    
    public String getPassword() {
        return passwordField.getText();
    }
    

}
