package com.xwlab.attendance.logic.model;

public class TemperatureInfo {
    private float[] temperatures;
    private float[] pixels;
    private float faceTemperature;
    private boolean isRealFace;

    public TemperatureInfo(float[] temperatures, float[] pixels, float faceTemperature, boolean isRealFace) {
        this.temperatures = temperatures;
        this.pixels = pixels;
        this.faceTemperature = faceTemperature;
        this.isRealFace = isRealFace;
    }

    public float[] getTemperatures() {
        return temperatures;
    }

    public float[] getPixels() {
        return pixels;
    }

    public float getFaceTemperature() {
        return faceTemperature;
    }

    public boolean isRealFace() {
        return isRealFace;
    }
}
