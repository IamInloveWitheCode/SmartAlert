package com.example.smartalert;

public class Emergency {
    private String description = "";
    private String emergency = "";
    private String latitude = "";
    private String longtitude = "";
    private String location = "";
    private String timestamp = "";

    @Override
    public String toString() {
        return "Emergency{" +
                "description='" + description + "/n" +
                ", emergency='" + emergency + "/n" +
                ", latitude='" + latitude + "/n" +
                ", longtitude='" + longtitude + "/n" +
                ", location='" + location + "/n" +
                ", timestamp='" + timestamp + "/n" +
                '}';
    }

    public Emergency(){

    }

    public Emergency(String Description, String Emergency, String Latitude, String Longtitude, String Location, String Timestamp)
    {
        this.description = Description;
        this.emergency = Emergency;
        this.latitude = Latitude;
        this.longtitude = Longtitude;
        this.location = Location;
        this.timestamp = Timestamp;
    }

    public String getDescription()
    {
        return description;
    }

    public String getEmergency()
    {
        return emergency;
    }

    public String getLatitude()
    {
        return latitude;
    }

    public String getLongtitude()
    {
        return longtitude;
    }

    public String getLocation()
    {
        return location;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

}
