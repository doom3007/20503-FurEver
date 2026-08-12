package com.furever.common.models;

import java.time.LocalDate;

public class Pet {
    private int petID;
    private String name;
    private int categoryID;
    private String categoryName;
    private int age;
    private String gender;
    private String description;
    private String imagePath;
    private String status;
    private LocalDate publishDate;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    
    public Pet() {
    }
    
    public Pet(String name, int categoryID, int age, String gender, String description, 
                String imagePath, String status, LocalDate publishDate, 
                String ownerName, String ownerPhone, String ownerEmail) {
        this.name = name;
        this.categoryID = categoryID;
        this.age = age;
        this.gender = gender;
        this.description = description;
        this.imagePath = imagePath;
        this.status = status;
        this.publishDate = publishDate;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.ownerEmail = ownerEmail;
    }
    
    public int getPetID() {
        return petID;
    }
    
    public void setPetID(int petID) {
        this.petID = petID;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getCategoryID() {
        return categoryID;
    }
    
    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDate getPublishDate() {
        return publishDate;
    }
    
    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public String getOwnerPhone() {
        return ownerPhone;
    }
    
    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }
    
    public String getOwnerEmail() {
        return ownerEmail;
    }
    
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
    
    @Override
    public String toString() {
        return name + " (" + categoryName + ", " + age + " שנים)";
    }
}
