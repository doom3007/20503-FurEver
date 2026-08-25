package com.furever.client.logic;

import com.furever.common.models.AdoptionRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for adoption request API communication
 * Handles adoption request creation, retrieval, approval, rejection, and status management
 * Manages communication with request endpoints and duplicate request handling
 */
public class AdoptionRequestClientService extends BaseClientService {
    
    public List<AdoptionRequest> getAllRequests() throws IOException {
        AdoptionRequest[] requests = httpClient.get("/requests", AdoptionRequest[].class);
        return arrayToList(requests);
    }
    
    public List<AdoptionRequest> getRequestsByUserEmail(String email) throws IOException {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        AdoptionRequest[] requests = httpClient.get("/requests/user/" + encodedEmail, AdoptionRequest[].class);
        return arrayToList(requests);
    }

    public List<AdoptionRequest> getRequestsForUserByEmail(String email) throws IOException {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        AdoptionRequest[] requests = httpClient.get("/requests/for-user/" + encodedEmail, AdoptionRequest[].class);
        return arrayToList(requests);
    }
    
    public AdoptionRequest getRequestById(int requestID) throws IOException {
        return httpClient.get("/requests/" + requestID, AdoptionRequest.class);
    }
    
    public boolean addRequest(AdoptionRequest request) throws IOException {
        AdoptionRequest result = httpClient.post("/requests", request, AdoptionRequest.class);
        return isSuccessful(result);
    }
    
    public boolean approveRequest(int requestID) throws IOException {
        String result = httpClient.put("/requests/" + requestID + "/approve", null, String.class);
        return isSuccessful(result);
    }
    
    public boolean rejectRequest(int requestID) throws IOException {
        String result = httpClient.put("/requests/" + requestID + "/reject", null, String.class);
        return isSuccessful(result);
    }
    
    public boolean deleteRequest(int requestID) throws IOException {
        String result = httpClient.delete("/requests/" + requestID, String.class);
        return isSuccessful(result);
    }
    
    public boolean setRequestStatus(int requestID, String status) throws IOException {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("status", status);
        String result = httpClient.put("/requests/" + requestID + "/status", requestData, String.class);
        return isSuccessful(result);
    }
}
