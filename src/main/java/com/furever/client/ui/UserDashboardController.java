package com.furever.client.ui;

import com.furever.client.FurEverApp;
import com.furever.client.logic.AdoptionRequestClientService;
import com.furever.client.logic.CategoryClientService;
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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller class for regular user dashboard interface
 * Manages functionality for regular user dashboard including:
 * - Viewing available pets for adoption
 * - Searching and filtering pets by various criteria
 * - Sending adoption requests to pet owners
 * - Managing adoption requests received from other users
 * - Adding new pet adoption listings
 * Automatic session validity checking and redirecting to login on session expiration
 */
public class UserDashboardController extends BaseDashboardController {
    
    @FXML
    private TabPane mainTabPane;
    
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
    private VBox filterVBox;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button viewDetailsButton;

    @FXML
    private Button sendRequestButton;

    @FXML
    private Button viewRequestDetailsButton;
    
    @FXML
    private Button approveRequestButton;
    
    @FXML
    private Button rejectRequestButton;
    
    private CategoryClientService categoryClientService;
    private AdoptionRequestClientService adoptionRequestClientService;
    private ObservableList<Pet> petsList;
    private ObservableList<AdoptionRequest> requestsList;
    
    @FXML
    public void initialize() {
        initializeBase();
        
        this.categoryClientService = new CategoryClientService();
        this.adoptionRequestClientService = new AdoptionRequestClientService();
        this.petsList = FXCollections.observableArrayList();
        this.requestsList = FXCollections.observableArrayList();
        
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
        
        // Add selection listener to enable/disable adoption button
        petsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                User currentUser = FurEverApp.getCurrentUser();
                if (currentUser != null && newSelection.getOwnerEmail().equals(currentUser.getEmail())) {
                    // User owns this pet - disable adoption button
                    sendRequestButton.setDisable(true);
                    sendRequestButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-opacity: 0.5;");
                    UIUtils.showInfo(statusLabel, "לא ניתן לאמץ את חיית המחמד שלך עצמך");
                } else {
                    // User doesn't own this pet - enable adoption button
                    sendRequestButton.setDisable(false);
                    sendRequestButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                }
            } else {
                // No selection - disable adoption button
                sendRequestButton.setDisable(true);
                sendRequestButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-opacity: 0.5;");
            }
        });
        
        // Initially disable the adoption button but make it visible
        sendRequestButton.setDisable(true);
        sendRequestButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-opacity: 0.5;");
        sendRequestButton.setVisible(true);
        
        petsTableView.setRowFactory(tv -> {
            TableRow<Pet> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Pet selectedPet = row.getItem();
                    handleViewDetails(selectedPet);
                }
            });
            
            // Visual indication for owned pets - update on each update
            row.itemProperty().addListener((obs, oldPet, newPet) -> {
                updateRowStyle(row, newPet);
            });
            
            // Handle selection state to maintain dark text visibility
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                Pet pet = row.getItem();
                if (pet != null) {
                    User currentUser = FurEverApp.getCurrentUser();
                    if (currentUser != null && pet.getOwnerEmail().equals(currentUser.getEmail())) {
                        // Keep dark text for owned pets regardless of selection state
                        row.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #8b0000;");
                    } else {
                        // Default selection styling for non-owned pets
                        row.setStyle("");
                    }
                } else {
                    row.setStyle("");
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
        
        // Add Enter key listeners for search fields
        searchNameField.setOnAction(event -> handleSearch());
        maxAgeField.setOnAction(event -> handleSearch());
        
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getText().equals("הבקשות שלי")) {
                loadRequests();
                viewDetailsButton.setVisible(false);
                sendRequestButton.setVisible(false);
                viewRequestDetailsButton.setVisible(true);
                updateOwnerButtonVisibility();
                filterVBox.setVisible(false);
            } else if (newTab != null && newTab.getText().equals("חיות מחמד")) {
                viewDetailsButton.setVisible(true);
                sendRequestButton.setVisible(true);
                // Reset button state for pets tab
                Pet selectedPet = petsTableView.getSelectionModel().getSelectedItem();
                if (selectedPet != null) {
                    User currentUser = FurEverApp.getCurrentUser();
                    if (currentUser != null && selectedPet.getOwnerEmail().equals(currentUser.getEmail())) {
                        sendRequestButton.setDisable(true);
                        sendRequestButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-opacity: 0.5;");
                    } else {
                        sendRequestButton.setDisable(false);
                        sendRequestButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                    }
                } else {
                    sendRequestButton.setDisable(true);
                    sendRequestButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-opacity: 0.5;");
                }
                viewRequestDetailsButton.setVisible(false);
                approveRequestButton.setVisible(false);
                rejectRequestButton.setVisible(false);
                filterVBox.setVisible(true);
            }
        });
        
        approveRequestButton.setVisible(false);
        rejectRequestButton.setVisible(false);
        viewRequestDetailsButton.setVisible(false);
        
        // Set initial visibility based on the default tab (pets tab)
        Tab initialTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (initialTab != null && initialTab.getText().equals("חיות מחמד")) {
            sendRequestButton.setVisible(true);
        } else {
            sendRequestButton.setVisible(false);
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
        loadDataWithHandling(() -> {
            List<Pet> pets = petClientService.getAllPets();
            petsList.clear();
            petsList.addAll(pets);
            
            // Apply styling to owned pets after loading
            User currentUser = FurEverApp.getCurrentUser();
            if (currentUser != null) {
                petsTableView.refresh(); // Refresh to apply row styling
            }
            
            return pets.size();
        }, "נמצאו ", "שגיאה בטעינת חיות מחמד: ", statusLabel);
    }

    private void loadRequests() {
        statusLabel.clear();
        try {
            User currentUser = FurEverApp.getCurrentUser();
            if (currentUser != null) {
                List<AdoptionRequest> requests = adoptionRequestClientService.getRequestsForUserByEmail(currentUser.getEmail());
                requestsList.clear();
                requestsList.addAll(requests);
                UIUtils.showInfo(statusLabel, "נמצאו " + requests.size() + " בקשות");
            }
        } catch (IOException e) {
            UIUtils.showError(statusLabel, e.getMessage());
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
    
    @FXML
    private void handleSendAdoptionRequest() {
        Pet selectedPet = petsTableView.getSelectionModel().getSelectedItem();
        if (selectedPet == null) {
            UIUtils.showError(statusLabel, "אנא בחר חיית מחמד");
            return;
        }
        
        showAdoptionRequestDialog(selectedPet);
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
        UIUtils.showRequestDetails(selectedRequest);
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
            stage.setScene(new Scene(root, 550, 650));
            stage.setResizable(false);
            stage.showAndWait();
            
            loadPets();
        } catch (IOException e) {
            UIUtils.showError(statusLabel, "שגיאה בפתיחת חלון הוספת מודעה: " + e.getMessage());
        }
    }
    
    private void showPetDetails(Pet pet) {
        UIUtils.showPetDetails(pet);
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
            stage.setScene(new Scene(root, 450, 400));
            stage.setResizable(false);
            stage.showAndWait();
            
            // Refresh pets after dialog closes
            loadPets();
        } catch (IOException e) {
            UIUtils.showError(statusLabel, "שגיאה בפתיחת חלון בקשת אימוץ");
        }
    }
    
    /**
     * Update row styling based on pet ownership
     * Owned pets are styled differently to indicate they cannot be adopted by the current user
     */
    private void updateRowStyle(TableRow<Pet> row, Pet pet) {
        if (pet != null) {
            User currentUser = FurEverApp.getCurrentUser();
            if (currentUser != null && pet.getOwnerEmail().equals(currentUser.getEmail())) {
                // Style owned pets with red background and dark text for better readability
                row.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #8b0000;");
            } else {
                row.setStyle("");
            }
        } else {
            row.setStyle("");
        }
    }
}
