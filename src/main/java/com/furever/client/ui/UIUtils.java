package com.furever.client.ui;

import com.furever.common.models.Pet;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import java.time.format.DateTimeFormatter;

public class UIUtils {
    
    public static void blinkError(Label label) {
        blinkErrorControl(label, label.getStyle());
    }
    
    public static void blinkError(TextArea textArea) {
        blinkErrorControl(textArea, textArea.getStyle());
    }
    
    public static void blinkError(Control control, String originalStyle) {
        blinkErrorControl(control, originalStyle);
    }
    
    private static void blinkErrorControl(Control control, String originalStyle) {
        String boldErrorStyle = "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, e -> control.setStyle(originalStyle)),
            new KeyFrame(Duration.millis(150), e -> control.setStyle(originalStyle + "; " + boldErrorStyle)),
            new KeyFrame(Duration.millis(300), e -> control.setStyle(originalStyle)),
            new KeyFrame(Duration.millis(450), e -> control.setStyle(originalStyle + "; " + boldErrorStyle)),
            new KeyFrame(Duration.millis(600), e -> control.setStyle(originalStyle))
        );
        timeline.play();
    }
    
    public static void showError(Label label, String message) {
        label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        label.setText(message);
        blinkError(label);
    }
    
    public static void showError(TextArea textArea, String message) {
        textArea.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        textArea.setText(message);
        blinkError(textArea);
    }
    
    public static void showSuccess(Label label, String message) {
        label.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
        label.setText(message);
    }
    
    public static void showSuccess(TextArea textArea, String message) {
        textArea.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
        textArea.setText(message);
    }
    
    public static void showInfo(Label label, String message) {
        label.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        label.setText(message);
    }
    
    public static void showInfo(TextArea textArea, String message) {
        textArea.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        textArea.setText(message);
    }
    
    /**
     * Show pet details in an alert dialog
     * @param pet The pet to display details for
     */
    public static void showPetDetails(Pet pet) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("פרטי חיית מחמד");
        alert.setHeaderText(pet.getName());

        String details = String.format(
            "מזהה: %d\n" +
            "קטגוריה: %s\n" +
            "גיל: %d\n" +
            "מגדר: %s\n" +
            "סטטוס: %s\n" +
            "תאריך פרסום: %s\n" +
            "שם בעלים: %s\n" +
            "טלפון בעלים: %s\n" +
            "אימייל בעלים: %s\n" +
            "תיאור: %s",
            pet.getPetID(),
            pet.getCategoryName(),
            pet.getAge(),
            pet.getGender(),
            pet.getStatus(),
            pet.getPublishDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            pet.getOwnerName(),
            pet.getOwnerPhone(),
            pet.getOwnerEmail(),
            pet.getDescription() != null ? pet.getDescription() : "אין תיאור"
        );

        alert.setContentText(details);
        alert.showAndWait();
    }
}
