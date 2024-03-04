package com.example.smartalert;

public class Alert {
    private String emID = "";
    private String token="";


    @Override
    public String toString() {
        return "Emergency{" +
                "emID='" + emID + "/n" +
                ", token='" + token + "/n" +
                '}';
    }

    // Update the constructor to accept a User object for userID
    public Alert(String emID) {
        this.emID = emID;

    }

    public String getEmID() {
        return emID;
    }
    public void setEmID(String emID) {
        this.emID = emID;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }


}
