package com.furever.common.models;

import java.time.LocalDate;

public class AdoptionRequest {
    private int requestID;
    private int petID;
    private String petName;
    private String ownerName;
    private String message;
    private LocalDate requestDate;
    private String requestStatus;
    private String requesterName;
    private String requesterPhone;
    private String requesterEmail;
    
    public AdoptionRequest() {
    }
    
    public AdoptionRequest(int petID, String message, LocalDate requestDate, 
                          String requestStatus, String requesterName, 
                          String requesterPhone, String requesterEmail) {
        this.petID = petID;
        this.message = message;
        this.requestDate = requestDate;
        this.requestStatus = requestStatus;
        this.requesterName = requesterName;
        this.requesterPhone = requesterPhone;
        this.requesterEmail = requesterEmail;
    }
    
    public int getRequestID() {
        return requestID;
    }
    
    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }
    
    public int getPetID() {
        return petID;
    }
    
    public void setPetID(int petID) {
        this.petID = petID;
    }
    
    public String getPetName() {
        return petName;
    }
    
    public void setPetName(String petName) {
        this.petName = petName;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDate getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }
    
    public String getRequestStatus() {
        return requestStatus;
    }
    
    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
    
    public String getRequesterName() {
        return requesterName;
    }
    
    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }
    
    public String getRequesterPhone() {
        return requesterPhone;
    }
    
    public void setRequesterPhone(String requesterPhone) {
        this.requesterPhone = requesterPhone;
    }
    
    public String getRequesterEmail() {
        return requesterEmail;
    }
    
    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }
    
    @Override
    public String toString() {
        return "בקשה מאת " + requesterName + " עבור " + petName;
    }
}
