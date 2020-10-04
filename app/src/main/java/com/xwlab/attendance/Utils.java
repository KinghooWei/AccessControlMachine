package com.xwlab.attendance;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Utils {
    private final static String TAG = "Utils";

    /**
     * 温度转换
     */
    public static String t1f(float temp) {
        return String.format(java.util.Locale.ENGLISH, "%.1f", temp);
    }

    /**
     * toast
     */
    public static void showToastInThread(String mag) {
        Looper.prepare();
        Toast.makeText(AttendanceApplication.context,mag,Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    public static void showToast(String mag) {
        Toast.makeText(AttendanceApplication.context,mag,Toast.LENGTH_SHORT).show();
    }

    public static void sendMessage(Handler handler, int what) {
        Message msg = new Message();
        msg.what = what;
        handler.sendMessage(msg);
    }

    /**
     * log
     */
    private static final int INFO = 3;
    private static final int level = INFO;
    public static void logV(String tag, String msg) {
        int VERBOSE = 1;
        if (level <= VERBOSE)
            Log.v(tag,msg);
    }
    public static void logD(String tag, String msg) {
        int DEBUG = 2;
        if (level <= DEBUG)
            Log.d(tag,msg);
    }
    public static void logI(String tag, String msg) {
        int INFO = 3;
        if (level <= INFO)
            Log.i(tag,msg);
    }
    public static void logW(String tag, String msg) {
        int WARN = 4;
        if (level <= WARN)
            Log.w(tag,msg);
    }
    public static void logE(String tag, String msg) {
        int ERROR = 5;
        if (level <= ERROR)
            Log.e(tag,msg);
    }

    /**
     * bitmap转base64
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();

                result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
