package com.furever.client.ui;

import com.furever.client.FurEverApp;
import com.furever.client.logic.AdoptionRequestClientService;
import com.furever.common.models.AdoptionRequest;
import com.furever.common.models.Pet;
import com.furever.common.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Controller for the adoption request dialog
 * Handles adoption request form with validation and submission
 * Manages user input for adoption requests and communicates with server
 */
public class AdoptionRequestController {
    
    @FXML
    private Label petNameLabel;
    
    @FXML
    private TextField requesterNameField;
    
    @FXML
    private TextField requesterPhoneField;
    
    @FXML
    private TextField requesterEmailField;
    
    @FXML
    private TextArea messageArea;
    
    @FXML
    private Label messageLabel;
    
    private Pet pet;
    private AdoptionRequestClientService adoptionRequestClientService;
    
    public AdoptionRequestController() {
        this.adoptionRequestClientService = new AdoptionRequestClientService();
    }
    
    public void setPet(Pet pet) {
        this.pet = pet;
        petNameLabel.setText("חיית מחמד: " + pet.getName());
        
        User currentUser = FurEverApp.getCurrentUser();
        if (currentUser != null) {
            requesterNameField.setText(currentUser.getFullName());
            requesterPhoneField.setText(currentUser.getPhone());
            requesterEmailField.setText(currentUser.getEmail());
            requesterNameField.setEditable(false);
            requesterPhoneField.setEditable(false);
            requesterEmailField.setEditable(false);
        }
    }
    
    @FXML
    private void handleSubmit() {
        String requesterName = requesterNameField.getText();
        String requesterPhone = requesterPhoneField.getText();
        String requesterEmail = requesterEmailField.getText();
        String message = messageArea.getText();
        
        if (requesterName.isEmpty() || requesterPhone.isEmpty() || requesterEmail.isEmpty()) {
            UIUtils.showError(messageLabel, "אנא מלא את כל השדות החובה");
            return;
        }
        
        if (!ValidationUtils.isValidPhone(requesterPhone)) {
            UIUtils.showError(messageLabel, ValidationUtils.getPhoneValidationError());
            return;
        }
        
        if (!ValidationUtils.isValidEmail(requesterEmail)) {
            UIUtils.showError(messageLabel, ValidationUtils.getEmailValidationError());
            return;
        }
        
        try {
            AdoptionRequest request = new AdoptionRequest();
            request.setPetID(pet.getPetID());
            request.setMessage(message.isEmpty() ? null : message);
            request.setRequestDate(LocalDate.now());
            request.setRequestStatus("ממתינה");
            request.setRequesterName(requesterName);
            request.setRequesterPhone(requesterPhone);
            request.setRequesterEmail(requesterEmail);
            
            System.err.println("CLIENT: Sending adoption request for pet " + pet.getPetID());
            System.err.println("CLIENT: Request details - Name: " + requesterName + ", Phone: " + requesterPhone + ", Email: " + requesterEmail);
            
            boolean success = adoptionRequestClientService.addRequest(request);
            if (success) {
                UIUtils.showSuccess(messageLabel, "בקשת האימוץ נשלחה בהצלחה!");
                
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    javafx.stage.Stage stage = (javafx.stage.Stage) petNameLabel.getScene().getWindow();
                    stage.close();
                });
            } else {
                UIUtils.showError(messageLabel, "שגיאה בשליחת הבקשה");
            }
        } catch (IOException e) {
            System.err.println("CLIENT: Error sending request: " + e.getMessage());
            e.printStackTrace();

            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("HTTP 500")) {
                try {

                    int jsonStart = errorMessage.indexOf("{");
                    int jsonEnd = errorMessage.lastIndexOf("}");
                    if (jsonStart != -1 && jsonEnd != -1) {
                        String jsonError = errorMessage.substring(jsonStart, jsonEnd + 1);

                        if (jsonError.contains("\"error\"")) {
                            int errorStart = jsonError.indexOf("\"error\"") + 8;
                            int errorEnd = jsonError.indexOf("\"", errorStart + 1);
                            if (errorStart != -1 && errorEnd != -1) {
                                String actualError = jsonError.substring(errorStart + 1, errorEnd);

                                actualError = URLDecoder.decode(actualError, StandardCharsets.UTF_8);
                                UIUtils.showError(messageLabel, actualError);
                                return;
                            }
                        }
                    }
                } catch (Exception parseException) {
                    System.err.println("CLIENT: Error parsing server response: " + parseException.getMessage());
                    UIUtils.showError(messageLabel, "שגיאה בשרת: " + errorMessage);
                    return;
                }
            }

            UIUtils.showError(messageLabel, errorMessage);
        }
    }
    
    @FXML
    private void handleCancel() {
        javafx.stage.Stage stage = (javafx.stage.Stage) petNameLabel.getScene().getWindow();
        stage.close();
    }
    

}
