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
import java.time.LocalDate;

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
            request.setRequestStatus("נשלחה");
            request.setRequesterName(requesterName);
            request.setRequesterPhone(requesterPhone);
            request.setRequesterEmail(requesterEmail);
            
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
            UIUtils.showError(messageLabel, e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        javafx.stage.Stage stage = (javafx.stage.Stage) petNameLabel.getScene().getWindow();
        stage.close();
    }
    

}
