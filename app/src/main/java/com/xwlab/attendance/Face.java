package com.xwlab.attendance;


public class Face {
    // 模型初始化
    public native boolean FaceModelInit(String faceDetectionModelPath);

    public native int[] FaceDetect(byte[] imageDate, int imageWidth, int imageHeight, int imageChannel);

    public native boolean FaceModelUnInit();

    public native double FaceRecognize(byte[] faceDate1, int w1, int h1, byte[] faceDate2, int w2, int h2);

    public native String FaceFeatureRestore(byte[] faceDate, int w, int h);

    static {
        System.loadLibrary("Face");

    }
}