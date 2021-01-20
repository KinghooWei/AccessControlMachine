package com.xwlab.attendance.logic.model;

public class RelativeRect {
    private float relativeLeft;
    private float relativeTop;
    private float relativeRight;
    private float relativeBottom;

    public RelativeRect(float relativeLeft,float relativeTop,float relativeRight,float relativeBottom) {
        this.relativeLeft = relativeLeft;
        this.relativeTop = relativeTop;
        this.relativeRight = relativeRight;
        this.relativeBottom = relativeBottom;
    }


    public float getRelativeLeft() {
        return relativeLeft;
    }

    public float getRelativeTop() {
        return relativeTop;
    }

    public float getRelativeRight() {
        return relativeRight;
    }

    public float getRelativeBottom() {
        return relativeBottom;
    }
}
