package com.xwlab.attendance;

import android.app.Application;
import android.content.Context;

import com.lztek.tools.irmeter.MLX906xx;

public class AttendanceApplication extends Application {

    public static Context context;
    public static MLX906xx MLX90640;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        MLX90640 = new MLX906xx();
    }
}
