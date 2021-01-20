package com.xwlab.attendance.logic.model;

import android.graphics.Bitmap;

public class EncryptFace {
    private Bitmap encryptFace;
    private Double key;

    public EncryptFace(Bitmap encryptFace,Double key){
        this.encryptFace=encryptFace;
        this.key=key;
    }

    public Bitmap getEncryptFace() {
        return encryptFace;
    }

    public Double getKey() {
        return key;
    }
}
