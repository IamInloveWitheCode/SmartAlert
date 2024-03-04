package com.example.smartalert;

public class Emergency {
    private String description = "";
    private String emergency = "";
    private String latitude = "";
    private String longitude = "";
    private String location = "";
    private String timestamp = "";
    private String userID = "";
    private String imageUrl = ""; // New field for image URL
    private int count=1;
    private String status="pending";
    private String id="";
    
    @Override
    public String toString() {
        return "Emergency{" +
                "id='"+id+"/n"+
                ",description='" + description + "/n" +
                ", emergency='" + emergency + "/n" +
                ", latitude='" + latitude + "/n" +
                ", longitude='" + longitude + "/n" +
                ", location='" + location + "/n" +
                ", timestamp='" + timestamp + "/n" +
                ", user='" + userID + "/n" +
                ", image='" + imageUrl + "/n" +
                ", count='" + count + "/n" +
                ", status='" + status + "/n" +
                '}';
    }

    public Emergency() {

    }

    // Update the constructor to accept a User object for userID
    public Emergency(String Id, String Description, String Emergency, String Latitude, String Longitude, String Location, String Timestamp, String UserID,String imageUrl) {
        this.id=id;
        this.description = Description;
        this.emergency = Emergency;
        this.latitude = Latitude;
        this.longitude = Longitude;
        this.location = Location;
        this.timestamp = Timestamp;
        this.userID = UserID;
        this.imageUrl = imageUrl;
        this.count=count;
        this.status=status;
    }


    public String getDescription() {
        return description;
    }

    public String getEmergency() {
        return emergency;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLocation() {
        return location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    // Getter method for count
    public int getCount() {
        return count;
    }

    // Setter method for count
    public void setCount(int count) {
        this.count = count;
    }
    public String getStatus(){return status;}
    public void setStatus(String status){this.status=status;}
    public String getId(){return id;}
    public void setId(String id){this.id=id;}
}

