package com.xwlab.attendance.logic.model;

import android.graphics.Bitmap;

public class EncryptFace {
    private Bitmap encryptFace;
    private String key;

    public EncryptFace(Bitmap encryptFace,String key){
        this.encryptFace=encryptFace;
        this.key=key;
    }

    public Bitmap getEncryptFace() {
        return encryptFace;
    }

    public String getKey() {
        return key;
    }
}
