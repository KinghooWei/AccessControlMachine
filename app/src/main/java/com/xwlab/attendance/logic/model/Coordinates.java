package com.xwlab.attendance.logic.model;

public class Coordinates {
    private float longitude;
    private float latitude;

    public Coordinates(float longitude,float latitude){
        this.longitude=longitude;
        this.latitude=latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }
}
