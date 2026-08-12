package com.furever.server.data;

import com.furever.common.models.AdoptionRequest;
import com.furever.common.models.Pet;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdoptionRequestDAO {
    
    private PetDAO petDAO;
    
    public AdoptionRequestDAO() {
        this.petDAO = new PetDAO();
    }
    
    public List<AdoptionRequest> getAllRequests() throws SQLException {
        List<AdoptionRequest> requests = new ArrayList<>();
        String query = "SELECT ar.*, p.name as petName, p.ownerName as ownerName FROM AdoptionRequest ar " +
                      "LEFT JOIN Pet p ON ar.petID = p.petID " +
                      "ORDER BY ar.requestDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        }
        return requests;
    }
    
    public List<AdoptionRequest> getRequestsByPetId(int petID) throws SQLException {
        List<AdoptionRequest> requests = new ArrayList<>();
        String query = "SELECT ar.*, p.name as petName, p.ownerName as ownerName FROM AdoptionRequest ar " +
                      "LEFT JOIN Pet p ON ar.petID = p.petID " +
                      "WHERE ar.petID = ? " +
                      "ORDER BY ar.requestDate DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, petID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        }
        return requests;
    }
    
    public List<AdoptionRequest> getRequestsByUserEmail(String email) throws SQLException {
        List<AdoptionRequest> requests = new ArrayList<>();
        String query = "SELECT ar.*, p.name as petName, p.ownerName as ownerName FROM AdoptionRequest ar " +
                      "LEFT JOIN Pet p ON ar.petID = p.petID " +
                      "WHERE ar.requesterEmail = ? " +
                      "ORDER BY ar.requestDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return requests;
    }

    public List<AdoptionRequest> getRequestsForUserByEmail(String email) throws SQLException {
        List<AdoptionRequest> requests = new ArrayList<>();
        String query = "SELECT ar.*, p.name as petName, p.ownerName as ownerName FROM AdoptionRequest ar " +
                      "LEFT JOIN Pet p ON ar.petID = p.petID " +
                      "WHERE ar.requesterEmail = ? OR p.ownerEmail = ? " +
                      "ORDER BY ar.requestDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, email);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return requests;
    }
    
    public AdoptionRequest getRequestById(int requestID) throws SQLException {
        String query = "SELECT ar.*, p.name as petName, p.ownerName as ownerName FROM AdoptionRequest ar " +
                      "LEFT JOIN Pet p ON ar.petID = p.petID " +
                      "WHERE ar.requestID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, requestID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractRequestFromResultSet(rs);
            }
        }
        return null;
    }
    
    public boolean addRequest(AdoptionRequest request) throws SQLException {
        String petCheckQuery = "SELECT COUNT(*) FROM Pet WHERE petID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement petCheckPstmt = conn.prepareStatement(petCheckQuery)) {
            petCheckPstmt.setInt(1, request.getPetID());
            ResultSet rs = petCheckPstmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new SQLException("חיית המחמד לא קיימת");
            }
        }
        
        String ownerCheckQuery = "SELECT COUNT(*) FROM Pet WHERE petID = ? AND ownerEmail = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ownerCheckPstmt = conn.prepareStatement(ownerCheckQuery)) {
            ownerCheckPstmt.setInt(1, request.getPetID());
            ownerCheckPstmt.setString(2, request.getRequesterEmail());
            ResultSet rs = ownerCheckPstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("לא ניתן לאמץ את חיית המחמד שלך עצמך");
            }
        }
        
        String checkQuery = "SELECT COUNT(*) FROM AdoptionRequest WHERE petID = ? AND requesterEmail = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkQuery)) {
            checkPstmt.setInt(1, request.getPetID());
            checkPstmt.setString(2, request.getRequesterEmail());
            ResultSet rs = checkPstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("כבר קיימת בקשת אימוץ ממך לחיית מחמד זו");
            }
        }
        
        String query = "INSERT INTO AdoptionRequest (petID, message, requestDate, requestStatus, " +
                      "requesterName, requesterPhone, requesterEmail) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, request.getPetID());
            pstmt.setString(2, request.getMessage());
            pstmt.setDate(3, Date.valueOf(request.getRequestDate()));
            pstmt.setString(4, request.getRequestStatus());
            pstmt.setString(5, request.getRequesterName());
            pstmt.setString(6, request.getRequesterPhone());
            pstmt.setString(7, request.getRequesterEmail());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    request.setRequestID(generatedKeys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }
    
    public boolean updateRequestStatus(int requestID, String status) throws SQLException {
        String query = "UPDATE AdoptionRequest SET requestStatus = ? WHERE requestID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, requestID);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean rejectOtherRequestsForPet(int petID, int excludeRequestID) throws SQLException {
        String query = "UPDATE AdoptionRequest SET requestStatus = 'נדחתה' " +
                      "WHERE petID = ? AND requestID != ? AND requestStatus = 'ממתינה'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, petID);
            pstmt.setInt(2, excludeRequestID);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean hasApprovedRequestForPet(int petID) throws SQLException {
        String query = "SELECT COUNT(*) FROM AdoptionRequest WHERE petID = ? AND requestStatus = 'אושרה'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, petID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    public boolean deleteRequest(int requestID) throws SQLException {
        String query = "DELETE FROM AdoptionRequest WHERE requestID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, requestID);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    private AdoptionRequest extractRequestFromResultSet(ResultSet rs) throws SQLException {
        AdoptionRequest request = new AdoptionRequest();
        request.setRequestID(rs.getInt("requestID"));
        request.setPetID(rs.getInt("petID"));
        request.setPetName(rs.getString("petName"));
        request.setMessage(rs.getString("message"));
        request.setRequestDate(rs.getDate("requestDate").toLocalDate());
        request.setRequestStatus(rs.getString("requestStatus"));
        request.setRequesterName(rs.getString("requesterName"));
        request.setRequesterPhone(rs.getString("requesterPhone"));
        request.setRequesterEmail(rs.getString("requesterEmail"));
        
        String ownerName = rs.getString("ownerName");
        request.setOwnerName(ownerName != null ? ownerName : "לא ידוע");
        return request;
    }
}
