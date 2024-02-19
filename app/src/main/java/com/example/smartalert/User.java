package com.example.smartalert;

public class User {

    private String email;

    private String userId;

    public User(String userId, String email) {
        this.email = email;
        this.userId = userId;
    }

    // Getters and setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

}
