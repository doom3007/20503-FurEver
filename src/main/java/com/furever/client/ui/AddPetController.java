package com.furever.client.ui;

import com.furever.client.FurEverApp;
import com.furever.client.logic.CategoryClientService;
import com.furever.client.logic.PetClientService;
import com.furever.common.models.Category;
import com.furever.common.models.Pet;
import com.furever.common.models.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class AddPetController {
    
    @FXML
    private TextField nameField;
    
    @FXML
    private ComboBox<Category> categoryComboBox;
    
    @FXML
    private TextField ageField;
    
    @FXML
    private ComboBox<String> genderComboBox;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private TextField imagePathField;
    
    @FXML
    private TextField ownerNameField;
    
    @FXML
    private TextField ownerPhoneField;
    
    @FXML
    private TextField ownerEmailField;
    
    @FXML
    private Label messageLabel;
    
    private CategoryClientService categoryClientService;
    private PetClientService petClientService;
    
    @FXML
    public void initialize() {
        this.categoryClientService = new CategoryClientService();
        this.petClientService = new PetClientService();
        
        User currentUser = FurEverApp.getCurrentUser();
        if (currentUser != null) {
            ownerNameField.setText(currentUser.getFullName());
            ownerPhoneField.setText(currentUser.getPhone());
            ownerEmailField.setText(currentUser.getEmail());
            ownerNameField.setEditable(false);
            ownerPhoneField.setEditable(false);
            ownerEmailField.setEditable(false);
        }
        
        genderComboBox.setItems(FXCollections.observableArrayList("זכר", "נקבה"));
        
        loadCategories();
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryClientService.getAllCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        } catch (IOException e) {
            UIUtils.showError(messageLabel, "שגיאה בטעינת קטגוריות: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSave() {
        String name = nameField.getText();
        Category selectedCategory = categoryComboBox.getValue();
        String ageText = ageField.getText();
        String gender = genderComboBox.getValue();
        String description = descriptionArea.getText();
        String imagePath = imagePathField.getText();
        String ownerName = ownerNameField.getText();
        String ownerPhone = ownerPhoneField.getText();
        String ownerEmail = ownerEmailField.getText();
        
        if (name.isEmpty() || selectedCategory == null || ageText.isEmpty() || 
            gender == null || ownerName.isEmpty() || ownerPhone.isEmpty() || ownerEmail.isEmpty()) {
            UIUtils.showError(messageLabel, "אנא מלא את כל השדות החובה");
            return;
        }
        
        if (!ValidationUtils.isValidPhone(ownerPhone)) {
            UIUtils.showError(messageLabel, ValidationUtils.getPhoneValidationError());
            return;
        }
        
        if (!ValidationUtils.isValidEmail(ownerEmail)) {
            UIUtils.showError(messageLabel, ValidationUtils.getEmailValidationError());
            return;
        }
        
        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age < 0) {
                UIUtils.showError(messageLabel, "גיל לא יכול להיות שלילי");
                return;
            }
        } catch (NumberFormatException e) {
            UIUtils.showError(messageLabel, "גיל חייב להיות מספר");
            return;
        }
        
        try {
            Pet pet = new Pet();
            pet.setName(name);
            pet.setCategoryID(selectedCategory.getCategoryID());
            pet.setAge(age);
            pet.setGender(gender);
            pet.setDescription(description.isEmpty() ? null : description);
            pet.setImagePath(imagePath.isEmpty() ? null : imagePath);
            pet.setStatus("זמינה");
            pet.setPublishDate(LocalDate.now());
            pet.setOwnerName(ownerName);
            pet.setOwnerPhone(ownerPhone);
            pet.setOwnerEmail(ownerEmail);
            
            if (petClientService.addPet(pet)) {
                UIUtils.showSuccess(messageLabel, "המודעה נוספה בהצלחה!");
                
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    javafx.stage.Stage stage = (javafx.stage.Stage) nameField.getScene().getWindow();
                    stage.close();
                });
            } else {
                UIUtils.showError(messageLabel, "שגיאה בהוספת המודעה");
            }
        } catch (IOException e) {
            UIUtils.showError(messageLabel, "שגיאה בהוספת המודעה: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        javafx.stage.Stage stage = (javafx.stage.Stage) nameField.getScene().getWindow();
        stage.close();
    }
    

}
