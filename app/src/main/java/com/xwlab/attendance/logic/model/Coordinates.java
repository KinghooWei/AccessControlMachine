package com.xwlab.attendance.logic.model;

public class Coordinates {
    private String longitude;
    private String latitude;

    public Coordinates(String longitude,String latitude){
        this.longitude=longitude;
        this.latitude=latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }
}
