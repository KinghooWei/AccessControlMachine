package com.xwlab.attendance;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Utils {
    private final static String TAG = "Utils";

    public static String t1f(float temp) {
        return String.format(java.util.Locale.ENGLISH, "%.1f", temp);
    }
}
