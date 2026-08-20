package com.furever.client.ui;

import com.furever.client.FurEverApp;
import com.furever.client.logic.AdoptionRequestClientService;
import com.furever.client.logic.PetClientService;
import com.furever.client.logic.UserClientService;
import com.furever.common.models.AdoptionRequest;
import com.furever.common.models.Pet;
import com.furever.common.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableRow;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller class for admin dashboard interface
 * Manages functionality for admin dashboard including:
 * - User management: viewing list of all registered users
 * - Pet management: viewing, deleting inappropriate ads, updating status to adopted
 * - Adoption request management: approving, rejecting, changing status, deleting requests
 * - Full access to all system information (admin privileges)
 * Automatic session validity checking and redirecting to login on session expiration
 */
public class AdminDashboardController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private TabPane mainTabPane;
    
    @FXML
    private TableView<User> usersTableView;
    
    @FXML
    private TableColumn<User, Integer> userIdColumn;
    
    @FXML
    private TableColumn<User, String> usernameColumn;
    
    @FXML
    private TableColumn<User, String> fullNameColumn;
    
    @FXML
    private TableColumn<User, String> emailColumn;
    
    @FXML
    private TableColumn<User, String> phoneColumn;
    
    @FXML
    private TableColumn<User, Boolean> isAdminColumn;
    
    @FXML
    private TableColumn<User, String> regDateColumn;
    
    @FXML
    private TextArea usersStatusLabel;
    
    @FXML
    private TableView<Pet> adminPetsTableView;
    
    @FXML
    private TableColumn<Pet, Integer> adminPetIdColumn;
    
    @FXML
    private TableColumn<Pet, String> adminPetNameColumn;
    
    @FXML
    private TableColumn<Pet, String> adminPetCategoryColumn;
    
    @FXML
    private TableColumn<Pet, Integer> adminPetAgeColumn;
    
    @FXML
    private TableColumn<Pet, String> adminPetStatusColumn;
    
    @FXML
    private TableColumn<Pet, String> adminPetOwnerColumn;
    
    @FXML
    private TextArea petsStatusLabel;
    
    @FXML
    private TableView<AdoptionRequest> requestsTableView;
    
    @FXML
    private TableColumn<AdoptionRequest, Integer> requestIdColumn;

    @FXML
    private TableColumn<AdoptionRequest, Integer> requestPetIdColumn;

    @FXML
    private TableColumn<AdoptionRequest, String> requestPetColumn;
    
    @FXML
    private TableColumn<AdoptionRequest, String> requesterColumn;
    
    @FXML
    private TableColumn<AdoptionRequest, String> requestStatusColumn;
    
    @FXML
    private TableColumn<AdoptionRequest, String> requestOwnerColumn;
    
    @FXML
    private TableColumn<AdoptionRequest, String> requestDateColumn;
    
    @FXML
    private TextArea requestsStatusLabel;
    
    private UserClientService userClientService;
    private PetClientService petClientService;
    private AdoptionRequestClientService adoptionRequestClientService;
    
    private ObservableList<User> usersList;
    private ObservableList<Pet> petsList;
    private ObservableList<AdoptionRequest> requestsList;
    private Timer sessionCheckTimer;
    
    @FXML
    public void initialize() {
        this.userClientService = new UserClientService();
        this.petClientService = new PetClientService();
        this.adoptionRequestClientService = new AdoptionRequestClientService();
        
        this.usersList = FXCollections.observableArrayList();
        this.petsList = FXCollections.observableArrayList();
        this.requestsList = FXCollections.observableArrayList();
        
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        isAdminColumn.setCellValueFactory(new PropertyValueFactory<>("admin"));
        regDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        
        usersTableView.setItems(usersList);
        
        adminPetIdColumn.setCellValueFactory(new PropertyValueFactory<>("petID"));
        adminPetNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        adminPetCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        adminPetAgeColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        adminPetStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        adminPetOwnerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        
        adminPetsTableView.setItems(petsList);

        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestID"));
        requestPetIdColumn.setCellValueFactory(new PropertyValueFactory<>("petID"));
        requestPetColumn.setCellValueFactory(new PropertyValueFactory<>("petName"));
        requesterColumn.setCellValueFactory(new PropertyValueFactory<>("requesterName"));
        requestOwnerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        requestStatusColumn.setCellValueFactory(new PropertyValueFactory<>("requestStatus"));
        requestDateColumn.setCellValueFactory(new PropertyValueFactory<>("requestDate"));

        requestsTableView.setItems(requestsList);
        
        adminPetsTableView.setRowFactory(tv -> {
            TableRow<Pet> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Pet selectedPet = row.getItem();
                    handleViewPetDetails(selectedPet);
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
        
        User currentUser = FurEverApp.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("שלום, " + currentUser.getFullName() + " (מנהל)");
        }
        
        startSessionChecker();
        
        loadAllData();
    }
    
    private void loadAllData() {
        loadUsers();
        loadPets();
        loadRequests();
    }
    
    private void loadUsers() {
        usersStatusLabel.clear();
        try {
            List<User> users = userClientService.getAllUsers();
            usersList.clear();
            usersList.addAll(users);
            UIUtils.showInfo(usersStatusLabel, "נטענו " + users.size() + " משתמשים");
        } catch (IOException e) {
            UIUtils.showError(usersStatusLabel, e.getMessage());
        }
    }
    
    private void loadPets() {
        petsStatusLabel.clear();
        try {
            List<Pet> pets = petClientService.getAllPets();
            petsList.clear();
            petsList.addAll(pets);
            UIUtils.showInfo(petsStatusLabel, "נטענו " + pets.size() + " חיות מחמד");
        } catch (IOException e) {
            UIUtils.showError(petsStatusLabel, e.getMessage());
        }
    }
    
    private void loadRequests() {
        requestsStatusLabel.clear();
        try {
            List<AdoptionRequest> requests = adoptionRequestClientService.getAllRequests();
            requestsList.clear();
            requestsList.addAll(requests);
            UIUtils.showInfo(requestsStatusLabel, "נטענו " + requests.size() + " בקשות");
        } catch (IOException e) {
            UIUtils.showError(requestsStatusLabel, e.getMessage());
        }
    }
    
    @FXML
    private void handleUsersTab() {
        mainTabPane.getSelectionModel().select(0);
    }
    
    @FXML
    private void handlePetsTab() {
        mainTabPane.getSelectionModel().select(1);
    }
    
    @FXML
    private void handleRequestsTab() {
        mainTabPane.getSelectionModel().select(2);
    }
    
    @FXML
    private void handleRefreshUsers() {
        loadUsers();
    }
    
    @FXML
    private void handleRefreshPets() {
        loadPets();
    }
    
    @FXML
    private void handleRefreshRequests() {
        loadRequests();
    }
    
    @FXML
    private void handleMarkAdopted() {
        petsStatusLabel.clear();
        Pet selectedPet = adminPetsTableView.getSelectionModel().getSelectedItem();
        if (selectedPet == null) {
            UIUtils.showError(petsStatusLabel, "אנא בחר חיית מחמד");
            return;
        }
        
        try {
            boolean success = petClientService.updatePetStatus(selectedPet.getPetID(), "אומצה");
            if (success) {
                petsStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                petsStatusLabel.setText("הסטטוס עודכן בהצלחה");
                loadPets();
            } else {
                UIUtils.showError(petsStatusLabel, "שגיאה בעדכון הסטטוס");
            }
        } catch (IOException e) {
            UIUtils.showError(petsStatusLabel, "שגיאה בעדכון הסטטוס: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeletePet() {
        petsStatusLabel.clear();
        Pet selectedPet = adminPetsTableView.getSelectionModel().getSelectedItem();
        if (selectedPet == null) {
            UIUtils.showError(petsStatusLabel, "אנא בחר חיית מחמד");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("מחיקת מודעה");
        alert.setHeaderText("האם אתה בטוח שברצונך למחוק את המודעה?");
        alert.setContentText("חיית מחמד: " + selectedPet.getName());
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = petClientService.deletePet(selectedPet.getPetID());
                    if (success) {
                        petsStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                        petsStatusLabel.setText("המודעה נמחקה בהצלחה");
                        loadPets();
                        adminPetsTableView.refresh();
                    } else {
                        UIUtils.showError(petsStatusLabel, "שגיאה במחיקת המודעה");
                    }
                } catch (IOException e) {
                    UIUtils.showError(petsStatusLabel, "שגיאה במחיקת המודעה: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleDeleteRequest() {
        requestsStatusLabel.clear();
        AdoptionRequest selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            UIUtils.showError(requestsStatusLabel, "אנא בחר בקשה");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("מחיקת בקשה");
        alert.setHeaderText("האם אתה בטוח שברצונך למחוק את הבקשה?");
        alert.setContentText("בקשה מאת: " + selectedRequest.getRequesterName());
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = adoptionRequestClientService.deleteRequest(selectedRequest.getRequestID());
                    if (success) {
                        requestsStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                        requestsStatusLabel.setText("הבקשה נמחקה בהצלחה");
                        loadRequests();
                        requestsTableView.refresh();
                    } else {
                        UIUtils.showError(requestsStatusLabel, "שגיאה במחיקת הבקשה");
                    }
                } catch (IOException e) {
                    UIUtils.showError(requestsStatusLabel, "שגיאה במחיקת הבקשה: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleSetPending() {
        requestsStatusLabel.clear();
        AdoptionRequest selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            UIUtils.showError(requestsStatusLabel, "אנא בחר בקשה");
            return;
        }
        
        try {
            boolean success = adoptionRequestClientService.setRequestStatus(selectedRequest.getRequestID(), "ממתינה");
            if (success) {
                requestsStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                requestsStatusLabel.setText("הסטטוס עודכן לממתינה");
                loadRequests();
                loadPets();
                requestsTableView.refresh();
                adminPetsTableView.refresh();
            } else {
                UIUtils.showError(requestsStatusLabel, "שגיאה בעדכון הסטטוס");
            }
        } catch (IOException e) {
            UIUtils.showError(requestsStatusLabel, "שגיאה בעדכון הסטטוס: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSetApproved() {
        requestsStatusLabel.clear();
        AdoptionRequest selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            UIUtils.showError(requestsStatusLabel, "אנא בחר בקשה");
            return;
        }
        
        try {
            boolean success = adoptionRequestClientService.setRequestStatus(selectedRequest.getRequestID(), "אושרה");
            if (success) {
                requestsStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                requestsStatusLabel.setText("הסטטוס עודכן למאושרת");
                loadRequests();
                loadPets();
                requestsTableView.refresh();
                adminPetsTableView.refresh();
            } else {
                UIUtils.showError(requestsStatusLabel, "שגיאה בעדכון הסטטוס");
            }
        } catch (IOException e) {
            UIUtils.showError(requestsStatusLabel, "שגיאה בעדכון הסטטוס: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSetRejected() {
        requestsStatusLabel.clear();
        AdoptionRequest selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            UIUtils.showError(requestsStatusLabel, "אנא בחר בקשה");
            return;
        }
        
        try {
            boolean success = adoptionRequestClientService.setRequestStatus(selectedRequest.getRequestID(), "נדחתה");
            if (success) {
                requestsStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                requestsStatusLabel.setText("הסטטוס עודכן לנדחית");
                loadRequests();
                loadPets();
                requestsTableView.refresh();
                adminPetsTableView.refresh();
            } else {
                UIUtils.showError(requestsStatusLabel, "שגיאה בעדכון הסטטוס");
            }
        } catch (IOException e) {
            UIUtils.showError(requestsStatusLabel, "שגיאה בעדכון הסטטוס: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewRequestDetails() {
        requestsStatusLabel.clear();
        AdoptionRequest selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            UIUtils.showError(requestsStatusLabel, "אנא בחר בקשה");
            return;
        }
        showRequestDetails(selectedRequest);
    }
    
    private void handleViewRequestDetails(AdoptionRequest request) {
        if (request == null) {
            UIUtils.showError(requestsStatusLabel, "אנא בחר בקשה");
            return;
        }
        showRequestDetails(request);
    }
    
    private void showRequestDetails(AdoptionRequest selectedRequest) {
        UIUtils.showRequestDetails(selectedRequest);
    }
    
    private void handleViewPetDetails(Pet pet) {
        if (pet == null) {
            UIUtils.showError(petsStatusLabel, "אנא בחר חיית מחמד");
            return;
        }
        showPetDetails(pet);
    }
    
    private void showPetDetails(Pet pet) {
        UIUtils.showPetDetails(pet);
    }
    
    private void startSessionChecker() {
        sessionCheckTimer = UIUtils.createSessionChecker(() -> {
            try {
                userClientService.getAllUsers();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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
            UIUtils.showError(usersStatusLabel, "שגיאה בהתנתקות: " + e.getMessage());
        }
    }
}