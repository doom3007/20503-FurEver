package com.furever.server.data;

import com.furever.common.models.Category;
import com.furever.common.models.Pet;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PetDAO {
    
    private CategoryDAO categoryDAO;
    
    public PetDAO() {
        this.categoryDAO = new CategoryDAO();
    }
    
    public List<Pet> getAllPets() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String query = "SELECT p.*, c.categoryName FROM Pet p " +
                      "LEFT JOIN Category c ON p.categoryID = c.categoryID " +
                      "ORDER BY p.publishDate DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                pets.add(extractPetFromResultSet(rs));
            }
        }
        return pets;
    }
    
    public List<Pet> getAvailablePets() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String query = "SELECT p.*, c.categoryName FROM Pet p " +
                      "LEFT JOIN Category c ON p.categoryID = c.categoryID " +
                      "WHERE p.status = 'זמינה' " +
                      "ORDER BY p.publishDate DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                pets.add(extractPetFromResultSet(rs));
            }
        }
        return pets;
    }
    
    public Pet getPetById(int petID) throws SQLException {
        String query = "SELECT p.*, c.categoryName FROM Pet p " +
                      "LEFT JOIN Category c ON p.categoryID = c.categoryID " +
                      "WHERE p.petID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, petID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractPetFromResultSet(rs);
            }
        }
        return null;
    }
    
    public List<Pet> getPetsByCategory(int categoryID) throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String query = "SELECT p.*, c.categoryName FROM Pet p " +
                      "LEFT JOIN Category c ON p.categoryID = c.categoryID " +
                      "WHERE p.categoryID = ? AND p.status = 'זמינה' " +
                      "ORDER BY p.publishDate DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, categoryID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                pets.add(extractPetFromResultSet(rs));
            }
        }
        return pets;
    }
    
    /**
     * Check if a user owns a specific pet
     * Used for authorization checks to ensure users can only modify their own pets
     * @param userEmail Email address of the user to check
     * @param petID ID of the pet to check ownership for
     * @return true if the user owns the pet, false otherwise
     */
    public boolean doesUserOwnPet(String userEmail, int petID) throws SQLException {
        String query = "SELECT COUNT(*) FROM Pet WHERE petID = ? AND ownerEmail = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, petID);
            pstmt.setString(2, userEmail);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Search for pets with optional filters
     * Dynamically builds SQL query based on provided parameters
     * Only includes search criteria that are not null/empty
     * @param name Pet name to search for (partial match)
     * @param categoryID Category ID to filter by
     * @param maxAge Maximum age to include (inclusive)
     * @param gender Gender to filter by
     * @return List of pets matching the search criteria
     */
    public List<Pet> searchPets(String name, Integer categoryID, Integer maxAge, String gender) throws SQLException {
        List<Pet> pets = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT p.*, c.categoryName FROM Pet p " +
            "LEFT JOIN Category c ON p.categoryID = c.categoryID " +
            "WHERE p.status = 'זמינה' "
        );
        
        List<Object> parameters = new ArrayList<>();
        
        if (name != null && !name.isEmpty()) {
            query.append("AND p.name LIKE ? ");
            parameters.add("%" + name + "%");
        }
        
        if (categoryID != null) {
            query.append("AND p.categoryID = ? ");
            parameters.add(categoryID);
        }
        
        if (maxAge != null) {
            query.append("AND p.age <= ? ");
            parameters.add(maxAge);
        }
        
        if (gender != null && !gender.isEmpty()) {
            query.append("AND p.gender = ? ");
            parameters.add(gender);
        }
        
        query.append("ORDER BY p.publishDate DESC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
            
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                pets.add(extractPetFromResultSet(rs));
            }
        }
        return pets;
    }
    
    public boolean addPet(Pet pet) throws SQLException {
        String query = "INSERT INTO Pet (name, categoryID, age, gender, description, imagePath, status, " +
                      "publishDate, ownerName, ownerPhone, ownerEmail) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, pet.getName());
            pstmt.setInt(2, pet.getCategoryID());
            pstmt.setInt(3, pet.getAge());
            pstmt.setString(4, pet.getGender());
            pstmt.setString(5, pet.getDescription());
            pstmt.setString(6, pet.getImagePath());
            pstmt.setString(7, pet.getStatus());
            pstmt.setDate(8, Date.valueOf(pet.getPublishDate()));
            pstmt.setString(9, pet.getOwnerName());
            pstmt.setString(10, pet.getOwnerPhone());
            pstmt.setString(11, pet.getOwnerEmail());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    pet.setPetID(generatedKeys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }
    
    public boolean updatePet(Pet pet) throws SQLException {
        String query = "UPDATE Pet SET name = ?, categoryID = ?, age = ?, gender = ?, " +
                      "description = ?, imagePath = ?, status = ?, ownerName = ?, " +
                      "ownerPhone = ?, ownerEmail = ? WHERE petID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, pet.getName());
            pstmt.setInt(2, pet.getCategoryID());
            pstmt.setInt(3, pet.getAge());
            pstmt.setString(4, pet.getGender());
            pstmt.setString(5, pet.getDescription());
            pstmt.setString(6, pet.getImagePath());
            pstmt.setString(7, pet.getStatus());
            pstmt.setString(8, pet.getOwnerName());
            pstmt.setString(9, pet.getOwnerPhone());
            pstmt.setString(10, pet.getOwnerEmail());
            pstmt.setInt(11, pet.getPetID());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean deletePet(int petID) throws SQLException {
        String query = "DELETE FROM Pet WHERE petID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, petID);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean updatePetStatus(int petID, String status) throws SQLException {
        String query = "UPDATE Pet SET status = ? WHERE petID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, petID);
            
            boolean result = pstmt.executeUpdate() > 0;

            if (result && status.equals("אומצה")) {
                String rejectQuery = "UPDATE AdoptionRequest SET requestStatus = 'נדחתה' WHERE petID = ? AND requestStatus = 'נשלחה'";
                try (PreparedStatement rejectPstmt = conn.prepareStatement(rejectQuery)) {
                    rejectPstmt.setInt(1, petID);
                    rejectPstmt.executeUpdate();
                }
            }

            return result;
        }
    }
    
    /**
     * Extract pet data from ResultSet row
     * Maps database columns to Pet object properties including category information
     * @param rs ResultSet containing pet data with category join
     * @return Pet object populated with data from ResultSet
     * @throws SQLException if database access error occurs
     */
    private Pet extractPetFromResultSet(ResultSet rs) throws SQLException {
        Pet pet = new Pet();
        pet.setPetID(rs.getInt("petID"));
        pet.setName(rs.getString("name"));
        pet.setCategoryID(rs.getInt("categoryID"));
        pet.setCategoryName(rs.getString("categoryName"));
        pet.setAge(rs.getInt("age"));
        pet.setGender(rs.getString("gender"));
        pet.setDescription(rs.getString("description"));
        pet.setImagePath(rs.getString("imagePath"));
        pet.setStatus(rs.getString("status"));
        pet.setPublishDate(rs.getDate("publishDate").toLocalDate());
        pet.setOwnerName(rs.getString("ownerName"));
        pet.setOwnerPhone(rs.getString("ownerPhone"));
        pet.setOwnerEmail(rs.getString("ownerEmail"));
        return pet;
    }
}
