package com.ravisharma.findfriend.Models;

public class User_location {
    String longitude;
    String latitude;

    public User_location() {
    }

    public User_location(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }
}
