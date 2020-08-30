package com.xwlab.attendance;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HttpService {
    private static final String TAG = "HttpService";
    private static String name = "";
    private static String personId = "";

    public static void reportAttendance(String name_, String personId_, final int encryption) {
        name = name_;
        personId = personId_;
        new Thread() {
            @Override
            public void run() {
                //-- 测试通用的api接口，application/json方式 --s//
                Log.i("HttpTest", "start->");
                JSONObject jsonObject = new JSONObject();
                String timestamp = HttpUtils.getSecondTimestamp();   //获取当前的时间戳
                try {
                    jsonObject.put("service", "door.attendance.enter");
                    jsonObject.put("name", name);
                    jsonObject.put("studentNum", personId);
                    jsonObject.put("timestamp", HttpUtils.getSecondTimestamp());
                    jsonObject.put("enterTime", HttpUtils.getSecondTimestamp());
                    jsonObject.put("encryption", encryption);
                    //updataTime_last 表示上次更新本地数据库的时间，本次会更新从这个时间之后新增的数据
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                String result = HttpUtils.sendJsonPost(jsonObject.toString());
                Log.i(TAG, result);
                //-- 测试通用的api接口，application/json方式 --e//
                //-- 测试通用的api接口，multipart/form-data方式 --e//
            }
        }.start();
    }

    public static void reportUnknown(Bitmap bitmap, final int encryption) {
        File sdDir = Environment.getExternalStorageDirectory();
        String tmpName = sdDir.toString() + "/unknown.jpg"; //临时图片文件
        final File file = new File(tmpName);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread() {
            @Override
            public void run() {
                Log.i("HttpTest", "start->");
                JSONObject jsonObject = new JSONObject();
                String timestamp = HttpUtils.getSecondTimestamp();   //获取当前的时间戳
                try {
                    jsonObject.put("service", "door.unknown.report");
                    jsonObject.put("enterTime", HttpUtils.getSecondTimestamp());
                    jsonObject.put("timestamp", HttpUtils.getSecondTimestamp());
                    jsonObject.put("encryption", encryption);
                    //updataTime_last 表示上次更新本地数据库的时间，本次会更新从这个时间之后新增的数据
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                String result = HttpUtils.sendMultiPartPost(file, jsonObject.toString());
                Log.i(TAG, "reportUnknown result is:" + result);
            }
        }.start();
    }

}
