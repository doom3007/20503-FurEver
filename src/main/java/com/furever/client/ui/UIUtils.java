package com.furever.client.ui;

import com.furever.client.FurEverApp;
import com.furever.client.logic.PetClientService;
import com.furever.client.logic.UserClientService;
import com.furever.common.models.AdoptionRequest;
import com.furever.common.models.Pet;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Utility class for UI helper methods
 * Provides common UI functionality including error display, animations, and pet details display
 * Handles message formatting and visual feedback for user interactions
 */
public class UIUtils {
    
    public static void blinkError(Label label) {
        String originalStyle = label.getStyle();
        String boldErrorStyle = "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, e -> label.setStyle(originalStyle)),
            new KeyFrame(Duration.millis(150), e -> label.setStyle(originalStyle + "; " + boldErrorStyle)),
            new KeyFrame(Duration.millis(300), e -> label.setStyle(originalStyle)),
            new KeyFrame(Duration.millis(450), e -> label.setStyle(originalStyle + "; " + boldErrorStyle)),
            new KeyFrame(Duration.millis(600), e -> label.setStyle(originalStyle))
        );
        timeline.play();
    }
    
    public static void blinkError(TextArea textArea) {
        String originalStyle = textArea.getStyle();
        String boldErrorStyle = "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, e -> textArea.setStyle(originalStyle)),
            new KeyFrame(Duration.millis(150), e -> textArea.setStyle(originalStyle + "; " + boldErrorStyle)),
            new KeyFrame(Duration.millis(300), e -> textArea.setStyle(originalStyle)),
            new KeyFrame(Duration.millis(450), e -> textArea.setStyle(originalStyle + "; " + boldErrorStyle)),
            new KeyFrame(Duration.millis(600), e -> textArea.setStyle(originalStyle))
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
        alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

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
    
    /**
     * Show adoption request details in an alert dialog
     * @param request The adoption request to display details for
     */
    public static void showRequestDetails(AdoptionRequest request) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("פרטי בקשה");
        alert.setHeaderText("בקשה מספר: " + request.getRequestID());
        alert.getDialogPane().setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);

        String details = String.format(
            "חיית מחמד: %s (מזהה: %d)\n" +
            "בעלים: %s\n" +
            "מבקש: %s\n" +
            "טלפון מבקש: %s\n" +
            "אימייל מבקש: %s\n" +
            "הודעה: %s\n" +
            "סטטוס: %s\n" +
            "תאריך: %s",
            request.getPetName(),
            request.getPetID(),
            request.getOwnerName(),
            request.getRequesterName(),
            request.getRequesterPhone(),
            request.getRequesterEmail(),
            request.getMessage() != null ? request.getMessage() : "אין הודעה",
            request.getRequestStatus(),
            request.getRequestDate()
        );

        alert.setContentText(details);
        alert.showAndWait();
    }
    
    /**
     * Create and start a session checker timer that validates authentication periodically
     * Automatically redirects to login screen if session expires
     * @param checkService Service to use for validation (PetClientService or UserClientService)
     * @return Timer instance for session checking
     */
    public static Timer createSessionChecker(Runnable checkService) {
        Timer sessionCheckTimer = new Timer(true);
        sessionCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    checkService.run();
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        try {
                            FurEverApp.clearAuth();
                            FurEverApp.showLoginScreen();
                        } catch (IOException ioException) {
                            System.err.println("שגיאה בהפניה למסך התחברות: " + ioException.getMessage());
                        }
                    });
                }
            }
        }, 30000, 30000);
        return sessionCheckTimer;
    }
}
