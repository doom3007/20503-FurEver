package com.furever.server.logic;

import com.furever.common.models.Pet;
import com.furever.server.data.PetDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for pet management
 * Provides business logic for managing pets including:
 * - Retrieving pets (all, available, by category, search)
 * - Adding new pets with validation
 * - Updating and deleting pets
 * - Updating pet status (available, in adoption process, adopted)
 * - Checking ownership of pets
 */
public class PetService extends BaseService {
    private PetDAO petDAO;
    
    public PetService() {
        this.petDAO = new PetDAO();
    }
    
    public List<Pet> getAllPets() throws SQLException {
        return petDAO.getAllPets();
    }
    
    public List<Pet> getAvailablePets() throws SQLException {
        return petDAO.getAvailablePets();
    }
    
    public Pet getPetById(int petID) throws SQLException {
        return petDAO.getPetById(petID);
    }
    
    public List<Pet> getPetsByCategory(int categoryID) throws SQLException {
        return petDAO.getPetsByCategory(categoryID);
    }
    
    public List<Pet> searchPets(String name, Integer categoryID, Integer maxAge, String gender) throws SQLException {
        return petDAO.searchPets(name, categoryID, maxAge, gender);
    }
    
    public boolean addPet(Pet pet) throws SQLException {
        validateNotNullOrEmpty(pet.getName(), "שם חיית המחמד");
        validatePositiveId(pet.getCategoryID(), "מזהה קטגוריה");
        validateNotNegative(pet.getAge(), "גיל");
        validateNotNullOrEmpty(pet.getOwnerName(), "שם הבעלים");
        validateNotNullOrEmpty(pet.getOwnerPhone(), "טלפון הבעלים");
        validateNotNullOrEmpty(pet.getOwnerEmail(), "אימייל הבעלים");
        
        return petDAO.addPet(pet);
    }
    
    public boolean updatePet(Pet pet) throws SQLException {
        validatePositiveId(pet.getPetID(), "מזהה חיית מחמד");
        return petDAO.updatePet(pet);
    }
    
    public boolean deletePet(int petID) throws SQLException {
        return petDAO.deletePet(petID);
    }
    
    public boolean markAsAdopted(int petID) throws SQLException {
        return petDAO.updatePetStatus(petID, "אומצה");
    }
    
    public boolean markAsInProgress(int petID) throws SQLException {
        return petDAO.updatePetStatus(petID, "בתהליך אימוץ");
    }
    
    public boolean markAsAvailable(int petID) throws SQLException {
        return petDAO.updatePetStatus(petID, "זמינה");
    }
    
    public boolean updatePetStatus(int petID, String status) throws SQLException {
        return petDAO.updatePetStatus(petID, status);
    }
    
    public boolean doesUserOwnPet(String userEmail, int petID) throws SQLException {
        return petDAO.doesUserOwnPet(userEmail, petID);
    }
}
