package com.furever.client.logic;

import com.furever.client.communication.HttpClient;
import com.furever.common.models.Pet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetClientService {
    private HttpClient httpClient;
    
    public PetClientService() {
        this.httpClient = new HttpClient();
    }
    
    public List<Pet> getAllPets() throws IOException {
        Pet[] pets = httpClient.get("/pets", Pet[].class);
        return Arrays.asList(pets);
    }
    
    public Pet getPetById(int petID) throws IOException {
        return httpClient.get("/pets/" + petID, Pet.class);
    }
    
    public List<Pet> getPetsByCategory(int categoryID) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("category", String.valueOf(categoryID));
        Pet[] pets = httpClient.get("/pets", params, Pet[].class);
        return Arrays.asList(pets);
    }
    
    public List<Pet> searchPets(String name, Integer categoryID, Integer maxAge, String gender) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (name != null && !name.isEmpty()) {
            params.put("name", name);
        }
        if (categoryID != null) {
            params.put("category", String.valueOf(categoryID));
        }
        if (maxAge != null) {
            params.put("maxAge", String.valueOf(maxAge));
        }
        if (gender != null && !gender.isEmpty()) {
            params.put("gender", gender);
        }
        Pet[] pets = httpClient.get("/pets/search", params, Pet[].class);
        return Arrays.asList(pets);
    }
    
    public boolean addPet(Pet pet) throws IOException {
        Pet result = httpClient.post("/pets", pet, Pet.class);
        return result != null;
    }
    
    public boolean updatePet(Pet pet) throws IOException {
        Pet result = httpClient.put("/pets/" + pet.getPetID(), pet, Pet.class);
        return result != null;
    }
    
    public boolean deletePet(int petID) throws IOException {
        String result = httpClient.delete("/pets/" + petID, String.class);
        return result != null;
    }
    
    public boolean updatePetStatus(int petID, String status) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        String result = httpClient.put("/pets/" + petID + "/status", body, String.class);
        return result != null;
    }
}
