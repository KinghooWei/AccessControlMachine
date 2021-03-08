package com.xwlab.attendance.logic.dao;

public class User {
    private String name;

    private String phoneNum;

    private double[] feature;

    private double[] featureWithMask;

    private String password;

    private boolean mask;

    public User(String name, String phoneNum, double[] feature, double[] featureWithMask, String password) {
        this.name = name;
        this.phoneNum = phoneNum;
        this.feature = feature;
        this.featureWithMask = featureWithMask;
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public double[] getFeature() {
        return feature;
    }

    public void setFeature(double[] feature) {
        this.feature = feature;
    }

    public double[] getFeatureWithMask() {
        return featureWithMask;
    }

    public void setFeatureWithMask(double[] featureWithMask) {
        this.featureWithMask = featureWithMask;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isMask() {
        return mask;
    }

    public void setMask(boolean mask) {
        this.mask = mask;
    }
}
