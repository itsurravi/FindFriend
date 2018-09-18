package com.ravisharma.findfriend.Models;

public class User_location {
    String longitude;
    String latitude;
    String status;
    String uid;

    public String getUid() {
        return uid;
    }

    public User_location() {
    }

    public User_location(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public User_location(String longitude, String latitude, String status, String uid) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.status = status;
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }
}
