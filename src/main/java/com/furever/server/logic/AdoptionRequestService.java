package com.furever.server.logic;

import com.furever.common.models.AdoptionRequest;
import com.furever.server.data.AdoptionRequestDAO;
import com.furever.server.logic.PetService;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for adoption request management
 * 
 * <p>This service provides business logic for managing adoption requests in the FurEver system.
 * It handles the complete lifecycle of adoption requests including creation, approval, rejection,
 * status management, and deletion with proper validation and state transitions.</p>
 * 
 * <p>Key business rules enforced:</p>
 * <ul>
 *   <li>Self-adoption prevention: Users cannot request to adopt their own pets</li>
 *   <li>Duplicate request prevention: Users cannot send multiple requests for the same pet</li>
 *   <li>Ownership validation: Only pet owners can approve/reject requests for their pets</li>
 *   <li>Exclusivity: Only one approved request per pet (others are automatically rejected)</li>
 *   <li>Status synchronization: Pet status updates when request status changes</li>
 * </ul>
 * 
 * <p>Request status workflow:</p>
 * <ul>
 *   <li>ממתינה (Pending) - Initial status for new requests</li>
 *   <li>אושרה (Approved) - Pet status changes to "אומצה" (Adopted)</li>
 *   <li>נדחתה (Rejected) - Pet remains available unless previously approved</li>
 * </ul>
 * 
 * @author FurEver Development Team
 * @version 1.0
 */
public class AdoptionRequestService extends BaseService {
    private AdoptionRequestDAO adoptionRequestDAO;
    private PetService petService;
    
    /**
     * Constructs a new AdoptionRequestService with required dependencies
     */
    public AdoptionRequestService() {
        this.adoptionRequestDAO = new AdoptionRequestDAO();
        this.petService = new PetService();
    }
    
    /**
     * Retrieve all adoption requests in the system
     * 
     * @return List of all adoption requests
     * @throws SQLException if database error occurs
     */
    public List<AdoptionRequest> getAllRequests() throws SQLException {
        return adoptionRequestDAO.getAllRequests();
    }
    
    /**
     * Retrieve a specific adoption request by ID
     * 
     * @param requestID The ID of the request to retrieve
     * @return The adoption request if found
     * @throws SQLException if database error occurs
     */
    public AdoptionRequest getRequestById(int requestID) throws SQLException {
        return adoptionRequestDAO.getRequestById(requestID);
    }
    
    /**
     * Retrieve all adoption requests for a specific pet
     * 
     * @param petID The ID of the pet
     * @return List of adoption requests for the specified pet
     * @throws SQLException if database error occurs
     */
    public List<AdoptionRequest> getRequestsByPetId(int petID) throws SQLException {
        return adoptionRequestDAO.getRequestsByPetId(petID);
    }
    
    /**
     * Retrieve all adoption requests made by a specific user (as requester)
     * 
     * @param email The email of the user who made the requests
     * @return List of adoption requests made by the specified user
     * @throws SQLException if database error occurs
     */
    public List<AdoptionRequest> getRequestsByUserEmail(String email) throws SQLException {
        return adoptionRequestDAO.getRequestsByUserEmail(email);
    }

    /**
     * Retrieve all adoption requests for pets owned by a specific user
     * 
     * @param email The email of the pet owner
     * @return List of adoption requests for the user's pets
     * @throws SQLException if database error occurs
     */
    public List<AdoptionRequest> getRequestsForUserByEmail(String email) throws SQLException {
        return adoptionRequestDAO.getRequestsForUserByEmail(email);
    }
    
    /**
     * Validate and add new adoption request
     * 
     * <p>This method validates the request data and enforces business rules:
     * Self-adoption prevention (requester cannot be pet owner) and duplicate
     * request prevention (user cannot request same pet twice) are handled at the DAO level.</p>
     * 
     * @param request Adoption request to add with required fields (petID, requester info)
     * @return true if request added successfully
     * @throws SQLException if validation fails or database error occurs
     */
    public boolean addRequest(AdoptionRequest request) throws SQLException {
        validatePositiveIdSql(request.getPetID(), "מזהה חיית מחמד");
        validateNotNullOrEmptySql(request.getRequesterName(), "שם המבקש");
        validateNotNullOrEmptySql(request.getRequesterPhone(), "מספר הטלפון");
        validateNotNullOrEmptySql(request.getRequesterEmail(), "כתובת האימייל");
        
        return adoptionRequestDAO.addRequest(request);
    }
    
    
    /**
     * Approve an adoption request and handle related state changes
     * This method performs a multi-step approval process:
     * 1. Validates that the approving user owns the pet
     * 2. Checks if there's already an approved request for this pet (prevents duplicate approvals)
     * 3. Rejects all other pending requests for the same pet (maintains exclusivity)
     * 4. Updates the request status to "approved" (אושרה)
     * 5. Updates the pet status to "adopted" (אומצה) to prevent further requests
     * 
     * @param requestID ID of the request to approve
     * @param userEmail Email of the user approving (must be pet owner)
     * @return true if approval succeeded, false if request not found
     * @throws SQLException if validation fails (not owner, duplicate approval) or database error occurs
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
     * 
     * <p>Validates that the user owns the pet before allowing rejection.
     * If the request was previously approved, the pet status is restored to "זמינה" (Available).</p>
     * 
     * @param requestID ID of the request to reject
     * @param userEmail Email of the user rejecting (must be pet owner)
     * @return true if rejection succeeded, false if request not found
     * @throws SQLException if validation fails (not owner) or database error occurs
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
     * 
     * <p>Permanently removes the adoption request from the database.
     * This operation is typically used for cleaning up old or test requests.</p>
     * 
     * @param requestID ID of the request to delete
     * @return true if deletion succeeded
     * @throws SQLException if database error occurs
     */
    public boolean deleteRequest(int requestID) throws SQLException {
        return adoptionRequestDAO.deleteRequest(requestID);
    }
    
    /**
     * Set custom status for an adoption request with automatic pet status synchronization
     * 
     * <p>Updates the request status and automatically syncs the pet status based on the change:
     * <ul>
     *   <li>If setting to "אושרה" (Approved): Pet status becomes "אומצה" (Adopted)</li>
     *   <li>If setting to "ממתינה" (Pending) or "נדחתה" (Rejected) from "אושרה": Pet status becomes "זמינה" (Available)</li>
     * </ul>
     * 
     * @param requestID ID of the request to update
     * @param status New status value (must be one of: ממתינה, אושרה, נדחתה)
     * @return true if update succeeded, false if request not found
     * @throws SQLException if validation fails (invalid status) or database error occurs
     */
    public boolean setRequestStatus(int requestID, String status) throws SQLException {
        String[] allowedStatuses = {"ממתינה", "אושרה", "נדחתה"};
        validateStatusSql(status, allowedStatuses, "סטטוס");
        
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
