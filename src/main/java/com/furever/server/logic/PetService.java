package com.furever.server.logic;

import com.furever.common.models.Pet;
import com.furever.server.data.PetDAO;

import java.sql.SQLException;
import java.util.List;

public class PetService {
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
        if (pet.getName() == null || pet.getName().isEmpty()) {
            throw new IllegalArgumentException("שם חיית המחמד לא יכול להיות ריק");
        }
        if (pet.getCategoryID() <= 0) {
            throw new IllegalArgumentException("מזהה קטגוריה לא תקין");
        }
        if (pet.getAge() < 0) {
            throw new IllegalArgumentException("גיל לא יכול להיות שלילי");
        }
        if (pet.getOwnerName() == null || pet.getOwnerName().isEmpty()) {
            throw new IllegalArgumentException("שם הבעלים לא יכול להיות ריק");
        }
        if (pet.getOwnerPhone() == null || pet.getOwnerPhone().isEmpty()) {
            throw new IllegalArgumentException("טלפון הבעלים לא יכול להיות ריק");
        }
        if (pet.getOwnerEmail() == null || pet.getOwnerEmail().isEmpty()) {
            throw new IllegalArgumentException("אימייל הבעלים לא יכול להיות ריק");
        }
        
        return petDAO.addPet(pet);
    }
    
    public boolean updatePet(Pet pet) throws SQLException {
        if (pet.getPetID() <= 0) {
            throw new IllegalArgumentException("מזהה חיית מחמד לא תקין");
        }
        
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
