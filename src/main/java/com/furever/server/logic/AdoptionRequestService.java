package com.furever.server.logic;

import com.furever.common.models.AdoptionRequest;
import com.furever.server.data.AdoptionRequestDAO;
import com.furever.server.logic.PetService;

import java.sql.SQLException;
import java.util.List;

public class AdoptionRequestService {
    private AdoptionRequestDAO adoptionRequestDAO;
    private PetService petService;
    
    public AdoptionRequestService() {
        this.adoptionRequestDAO = new AdoptionRequestDAO();
        this.petService = new PetService();
    }
    
    public List<AdoptionRequest> getAllRequests() throws SQLException {
        return adoptionRequestDAO.getAllRequests();
    }
    
    public AdoptionRequest getRequestById(int requestID) throws SQLException {
        return adoptionRequestDAO.getRequestById(requestID);
    }
    
    public List<AdoptionRequest> getRequestsByPetId(int petID) throws SQLException {
        return adoptionRequestDAO.getRequestsByPetId(petID);
    }
    
    public List<AdoptionRequest> getRequestsByUserEmail(String email) throws SQLException {
        return adoptionRequestDAO.getRequestsByUserEmail(email);
    }

    public List<AdoptionRequest> getRequestsForUserByEmail(String email) throws SQLException {
        return adoptionRequestDAO.getRequestsForUserByEmail(email);
    }
    
    /**
     * Validate and add new adoption request
     * Prevents self-adoption and duplicate requests for same pet
     * @param request Adoption request to add
     * @return true if request added successfully
     * @throws SQLException if validation fails or database error occurs
     */
    public boolean addRequest(AdoptionRequest request) throws SQLException {
        if (request.getPetID() <= 0) {
            throw new SQLException("מזהה חיית מחמד לא תקין");
        }
        if (request.getRequesterName() == null || request.getRequesterName().isEmpty()) {
            throw new SQLException("שם המבקש לא יכול להיות ריק");
        }
        if (request.getRequesterPhone() == null || request.getRequesterPhone().isEmpty()) {
            throw new SQLException("מספר הטלפון לא יכול להיות ריק");
        }
        if (request.getRequesterEmail() == null || request.getRequesterEmail().isEmpty()) {
            throw new SQLException("כתובת האימייל לא יכולה להיות ריקה");
        }
        
        return adoptionRequestDAO.addRequest(request);
    }
    
    
    /**
     * Approve an adoption request and handle related state changes
     * - Validates pet ownership
     * - Checks for existing approved requests
     * - Rejects other pending requests for the same pet
     * - Updates pet status to adopted
     * @param requestID ID of the request to approve
     * @param userEmail Email of the user approving (must be pet owner)
     * @return true if approval succeeded
     * @throws SQLException if validation fails or database error occurs
     */
    public boolean approveRequest(int requestID, String userEmail) throws SQLException {
        AdoptionRequest request = adoptionRequestDAO.getRequestById(requestID);
        if (request != null) {
            if (!petService.doesUserOwnPet(userEmail, request.getPetID())) {
                throw new SQLException("אין לך הרשאה לאשר בקשה זו");
            }
            if (adoptionRequestDAO.hasApprovedRequestForPet(request.getPetID())) {
                throw new SQLException("כבר קיימת בקשה מאושרת לחיית מחמד זו");
            }
            adoptionRequestDAO.rejectOtherRequestsForPet(request.getPetID(), requestID);
            boolean requestUpdated = adoptionRequestDAO.updateRequestStatus(requestID, "אושרה");
            if (requestUpdated) {
                petService.updatePetStatus(request.getPetID(), "אומצה");
            }
            return requestUpdated;
        }
        return false;
    }
    
    /**
     * Reject an adoption request
     * Validates that the user owns the pet before allowing rejection
     * @param requestID ID of the request to reject
     * @param userEmail Email of the user rejecting (must be pet owner)
     * @return true if rejection succeeded
     * @throws SQLException if validation fails or database error occurs
     */
    public boolean rejectRequest(int requestID, String userEmail) throws SQLException {
        AdoptionRequest request = adoptionRequestDAO.getRequestById(requestID);
        if (request != null) {
            if (!petService.doesUserOwnPet(userEmail, request.getPetID())) {
                throw new SQLException("אין לך הרשאה לדחות בקשה זו");
            }
            if (request.getRequestStatus().equals("אושרה")) {
                petService.updatePetStatus(request.getPetID(), "זמינה");
            }
            return adoptionRequestDAO.updateRequestStatus(requestID, "נדחתה");
        }
        return false;
    }
    
    /**
     * Delete an adoption request
     * @param requestID ID of the request to delete
     * @return true if deletion succeeded
     * @throws SQLException if database error occurs
     */
    public boolean deleteRequest(int requestID) throws SQLException {
        return adoptionRequestDAO.deleteRequest(requestID);
    }
    
    /**
     * Set custom status for an adoption request
     * @param requestID ID of the request to update
     * @param status New status value
     * @return true if update succeeded
     * @throws SQLException if validation fails or database error occurs
     */
    public boolean setRequestStatus(int requestID, String status) throws SQLException {
        if (status == null || status.isEmpty()) {
            throw new SQLException("סטטוס לא יכול להיות ריק");
        }
        if (!status.equals("ממתינה") && !status.equals("אושרה") && !status.equals("נדחתה")) {
            throw new SQLException("סטטוס לא תקין");
        }
        
        AdoptionRequest request = adoptionRequestDAO.getRequestById(requestID);
        if (request == null) {
            return false;
        }
        
        boolean requestUpdated = adoptionRequestDAO.updateRequestStatus(requestID, status);
        
        if (requestUpdated) {
            if (status.equals("אושרה")) {
                petService.updatePetStatus(request.getPetID(), "אומצה");
            } else if (status.equals("ממתינה") || status.equals("נדחתה")) {
                if (request.getRequestStatus().equals("אושרה")) {
                    petService.updatePetStatus(request.getPetID(), "זמינה");
                }
            }
        }
        
        return requestUpdated;
    }
}
