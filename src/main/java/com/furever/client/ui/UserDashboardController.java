package com.furever.client.ui;

import com.furever.client.FurEverApp;
import com.furever.client.logic.AdoptionRequestClientService;
import com.furever.client.logic.CategoryClientService;
import com.furever.client.logic.PetClientService;
import com.furever.common.models.AdoptionRequest;
import com.furever.common.models.Category;
import com.furever.common.models.Pet;
import com.furever.common.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ButtonBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class UserDashboardController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private TextField searchNameField;
    
    @FXML
    private ComboBox<Category> categoryComboBox;
    
    @FXML
    private TextField maxAgeField;
    
    @FXML
    private ComboBox<String> genderComboBox;
    
    @FXML
    private TableView<Pet> petsTableView;
    
    @FXML
    private TableColumn<Pet, String> nameColumn;
    
    @FXML
    private TableColumn<Pet, String> categoryColumn;
    
    @FXML
    private TableColumn<Pet, Integer> ageColumn;
    
    @FXML
    private TableColumn<Pet, String> genderColumn;
    
    @FXML
    private TableColumn<Pet, String> statusColumn;
    
    @FXML
    private TableColumn<Pet, LocalDate> dateColumn;
    
    @FXML
    private TabPane mainTabPane;
    
    @FXML
    private TableView<AdoptionRequest> requestsTableView;

    @FXML
    private TableColumn<AdoptionRequest, Integer> requestPetIdColumn;

    @FXML
    private TableColumn<AdoptionRequest, String> requestPetColumn;
    
    @FXML
    private TableColumn<AdoptionRequest, String> requestOwnerColumn;
    
    @FXML
    private TableColumn<AdoptionRequest, String> requesterNameColumn;
    
    @FXML
    private TableColumn<AdoptionRequest, String> requestStatusColumn;
    
    @FXML
    private TableColumn<AdoptionRequest, LocalDate> requestDateColumn;
    
    @FXML
    private TextArea statusLabel;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button viewRequestDetailsButton;
    
    @FXML
    private Button approveRequestButton;
    
    @FXML
    private Button rejectRequestButton;
    
    private PetClientService petClientService;
    private CategoryClientService categoryClientService;
    private AdoptionRequestClientService adoptionRequestClientService;
    private ObservableList<Pet> petsList;
    private ObservableList<AdoptionRequest> requestsList;
    private Timer sessionCheckTimer;
    
    @FXML
    public void initialize() {
        this.petClientService = new PetClientService();
        this.categoryClientService = new CategoryClientService();
        this.adoptionRequestClientService = new AdoptionRequestClientService();
        this.petsList = FXCollections.observableArrayList();
        this.requestsList = FXCollections.observableArrayList();
        
        startSessionChecker();
        
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("publishDate"));
        
        requestPetIdColumn.setCellValueFactory(new PropertyValueFactory<>("petID"));
        requestPetColumn.setCellValueFactory(new PropertyValueFactory<>("petName"));
        requestOwnerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        requesterNameColumn.setCellValueFactory(new PropertyValueFactory<>("requesterName"));
        requestStatusColumn.setCellValueFactory(new PropertyValueFactory<>("requestStatus"));
        requestDateColumn.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateColumn.setCellFactory(column -> new TableCell<Pet, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(formatter));
                }
            }
        });
        
        requestDateColumn.setCellFactory(column -> new TableCell<AdoptionRequest, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(formatter));
                }
            }
        });
        
        petsTableView.setItems(petsList);
        requestsTableView.setItems(requestsList);
        
        petsTableView.setRowFactory(tv -> {
            TableRow<Pet> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Pet selectedPet = row.getItem();
                    handleViewDetails(selectedPet);
                }
            });
            return row;
        });
        
        requestsTableView.setRowFactory(tv -> {
            TableRow<AdoptionRequest> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    AdoptionRequest selectedRequest = row.getItem();
                    handleViewRequestDetails(selectedRequest);
                }
            });
            return row;
        });
        
        genderComboBox.setItems(FXCollections.observableArrayList("", "זכר", "נקבה"));
        
        loadCategories();
        loadPets();
        loadRequests();
        
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getText().equals("הבקשות שלי")) {
                loadRequests();
                viewDetailsButton.setVisible(false);
                viewRequestDetailsButton.setVisible(true);
                updateOwnerButtonVisibility();
            } else if (newTab != null && newTab.getText().equals("חיות מחמד")) {
                viewDetailsButton.setVisible(true);
                viewRequestDetailsButton.setVisible(false);
                approveRequestButton.setVisible(false);
                rejectRequestButton.setVisible(false);
            }
        });
        
        approveRequestButton.setVisible(false);
        rejectRequestButton.setVisible(false);
        viewRequestDetailsButton.setVisible(false);
        
        User currentUser = FurEverApp.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("שלום, " + currentUser.getFullName());
        }
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryClientService.getAllCategories();
            ObservableList<Category> categoriesWithEmpty = FXCollections.observableArrayList();
            categoriesWithEmpty.add(new Category(0, ""));
            categoriesWithEmpty.addAll(categories);
            categoryComboBox.setItems(categoriesWithEmpty);
        } catch (IOException e) {
            UIUtils.showError(statusLabel, "שגיאה בטעינת קטגוריות: " + e.getMessage());
        }
    }
    
    private void loadPets() {
        statusLabel.clear();
        try {
            List<Pet> pets = petClientService.getAllPets();
            petsList.clear();
            petsList.addAll(pets);
        } catch (IOException e) {
            if (e.getMessage().contains("התחברות פגה")) {
                UIUtils.showError(statusLabel, "ההתחברות פגה - אנא התחבר מחדש");
            } else {
                UIUtils.showError(statusLabel, "שגיאה בטעינת חיות מחמד: " + e.getMessage());
            }
        }
    }

    private void loadRequests() {
        statusLabel.clear();
        try {
            User currentUser = FurEverApp.getCurrentUser();
            if (currentUser != null) {
                List<AdoptionRequest> requests = adoptionRequestClientService.getRequestsForUserByEmail(currentUser.getEmail());
                requestsList.clear();
                requestsList.addAll(requests);
            }
        } catch (IOException e) {
            if (e.getMessage().contains("התחברות פגה")) {
                UIUtils.showError(statusLabel, "ההתחברות פגה - אנא התחבר מחדש");
            } else {
                UIUtils.showError(statusLabel, "שגיאה בטעינת בקשות: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSearch() {
        statusLabel.clear();
        String name = searchNameField.getText();
        Category selectedCategory = categoryComboBox.getValue();
        Integer categoryID = null;
        if (selectedCategory != null && selectedCategory.getCategoryID() != 0) {
            categoryID = selectedCategory.getCategoryID();
        }
        
        Integer maxAge = null;
        if (!maxAgeField.getText().isEmpty()) {
            try {
                maxAge = Integer.parseInt(maxAgeField.getText());
            } catch (NumberFormatException e) {
                UIUtils.showError(statusLabel, "גיל מקסימלי חייב להיות מספר");
                return;
            }
        }
        
        String gender = genderComboBox.getValue();
        if (gender != null && gender.isEmpty()) {
            gender = null;
        }
        
        try {
            List<Pet> pets = petClientService.searchPets(name, categoryID, maxAge, gender);
            petsList.clear();
            petsList.addAll(pets);
            UIUtils.showInfo(statusLabel, "נמצאו " + pets.size() + " חיות מחמד");
        } catch (IOException e) {
            UIUtils.showError(statusLabel, "שגיאה בחיפוש: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearFilter() {
        searchNameField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        maxAgeField.clear();
        genderComboBox.getSelectionModel().clearSelection();
        statusLabel.clear();
        loadPets();
    }
    
    @FXML
    private void handleViewDetails() {
        Pet selectedPet = petsTableView.getSelectionModel().getSelectedItem();
        if (selectedPet == null) {
            UIUtils.showError(statusLabel, "אנא בחר חיית מחמד");
            return;
        }

        showPetDetails(selectedPet);
    }
    
    private void handleViewDetails(Pet pet) {
        if (pet == null) {
            UIUtils.showError(statusLabel, "אנא בחר חיית מחמד");
            return;
        }
        showPetDetails(pet);
    }

    @FXML
    private void handleViewRequestDetails() {
        AdoptionRequest selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            UIUtils.showError(statusLabel, "אנא בחר בקשה");
            return;
        }
        showRequestDetails(selectedRequest);
    }
    
    private void handleViewRequestDetails(AdoptionRequest request) {
        if (request == null) {
            UIUtils.showError(statusLabel, "אנא בחר בקשה");
            return;
        }
        showRequestDetails(request);
    }
    
    private void showRequestDetails(AdoptionRequest selectedRequest) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("פרטי בקשה");
        alert.setHeaderText("בקשה מספר: " + selectedRequest.getRequestID());

        String details = String.format(
            "חיית מחמד: %s (מזהה: %d)\n" +
            "בעלים: %s\n" +
            "מבקש: %s\n" +
            "טלפון מבקש: %s\n" +
            "אימייל מבקש: %s\n" +
            "הודעה: %s\n" +
            "סטטוס: %s\n" +
            "תאריך: %s",
            selectedRequest.getPetName(),
            selectedRequest.getPetID(),
            selectedRequest.getOwnerName(),
            selectedRequest.getRequesterName(),
            selectedRequest.getRequesterPhone(),
            selectedRequest.getRequesterEmail(),
            selectedRequest.getMessage() != null ? selectedRequest.getMessage() : "אין הודעה",
            selectedRequest.getRequestStatus(),
            selectedRequest.getRequestDate()
        );

        alert.setContentText(details);
        alert.showAndWait();
    }
    
    @FXML
    private void handleApproveRequest() {
        AdoptionRequest selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            UIUtils.showError(statusLabel, "אנא בחר בקשה");
            return;
        }
        
        try {
            boolean success = adoptionRequestClientService.approveRequest(selectedRequest.getRequestID());
            if (success) {
                UIUtils.showSuccess(statusLabel, "הבקשה אושרה בהצלחה");
                loadRequests();
                loadPets();
                petsTableView.refresh();
            } else {
                UIUtils.showError(statusLabel, "נכשל באישור הבקשה");
            }
        } catch (IOException e) {
            UIUtils.showError(statusLabel, "שגיאה באישור הבקשה: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRejectRequest() {
        AdoptionRequest selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            UIUtils.showError(statusLabel, "אנא בחר בקשה");
            return;
        }
        
        try {
            boolean success = adoptionRequestClientService.rejectRequest(selectedRequest.getRequestID());
            if (success) {
                UIUtils.showSuccess(statusLabel, "הבקשה נדחתה בהצלחה");
                loadRequests();
                loadPets();
                petsTableView.refresh();
            } else {
                UIUtils.showError(statusLabel, "נכשל בדחיית הבקשה");
            }
        } catch (IOException e) {
            UIUtils.showError(statusLabel, "שגיאה בדחיית הבקשה: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadPets();
        loadRequests();
        UIUtils.showSuccess(statusLabel, "רענן בהצלחה");
    }
    
    private void updateOwnerButtonVisibility() {
        User currentUser = FurEverApp.getCurrentUser();
        if (currentUser != null) {
            approveRequestButton.setVisible(true);
            rejectRequestButton.setVisible(true);
        } else {
            approveRequestButton.setVisible(false);
            rejectRequestButton.setVisible(false);
        }
    }

    @FXML
    private void handleAddPet() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-pet.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("הוספת מודעת מסירה");
            stage.setScene(new Scene(root, 500, 600));
            stage.showAndWait();
            
            loadPets();
        } catch (IOException e) {
            UIUtils.showError(statusLabel, "שגיאה בפתיחת חלון הוספת מודעה: " + e.getMessage());
        }
    }
    
    /**
     * Start a background timer to check session validity every 30 seconds
     * Automatically redirects to login screen if session expires
     */
    private void startSessionChecker() {
        sessionCheckTimer = new Timer(true);
        sessionCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    petClientService.getAllPets();
                } catch (IOException e) {
                    if (e.getMessage().contains("התחברות פגה")) {
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
            }
        }, 30000, 30000);
    }
    
    @FXML
    private void handleLogout() {
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
    
    private void showPetDetails(Pet pet) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("פרטי חיית מחמד");
        alert.setHeaderText(pet.getName());

        String details = String.format(
            "קטגוריה: %s\n" +
            "גיל: %d שנים\n" +
            "מין: %s\n" +
            "סטטוס: %s\n" +
            "תיאור: %s\n\n" +
            "פרטי בעלים:\n" +
            "שם: %s\n" +
            "טלפון: %s\n" +
            "אימייל: %s\n" +
            "תאריך פרסום: %s",
            pet.getCategoryName(),
            pet.getAge(),
            pet.getGender(),
            pet.getStatus(),
            pet.getDescription() != null ? pet.getDescription() : "אין תיאור",
            pet.getOwnerName(),
            pet.getOwnerPhone(),
            pet.getOwnerEmail(),
            pet.getPublishDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        alert.setContentText(details);

        User currentUser = FurEverApp.getCurrentUser();
        if (currentUser != null && !pet.getOwnerEmail().equals(currentUser.getEmail()) && pet.getStatus().equals("זמינה")) {
            ButtonType requestButtonType = new ButtonType("בקש אימוץ", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(requestButtonType, ButtonType.CANCEL);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == requestButtonType) {
                showAdoptionRequestDialog(pet);
            }
        } else {
            alert.showAndWait();
        }
    }
    
    private void showAdoptionRequestDialog(Pet pet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/adoption-request.fxml"));
            Parent root = loader.load();
            AdoptionRequestController controller = loader.getController();
            controller.setPet(pet);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("בקשת אימוץ - " + pet.getName());
            stage.setScene(new Scene(root, 400, 350));
            stage.showAndWait();
        } catch (IOException e) {
            UIUtils.showError(statusLabel, "שגיאה בפתיחת חלון בקשת אימוץ");
        }
    }
}
