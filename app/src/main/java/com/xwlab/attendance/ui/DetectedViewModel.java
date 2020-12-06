package com.xwlab.attendance.ui;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.xwlab.attendance.HttpUtils;
import com.xwlab.attendance.Logger;
import com.xwlab.attendance.logic.Repository;
import com.xwlab.attendance.logic.model.RelativeRect;
import com.xwlab.attendance.logic.model.TemperatureInfo;
import com.xwlab.util.Constant;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetectedViewModel extends ViewModel {
    private static final String TAG = "DetectedViewModel";
    public MutableLiveData<String> expression = new MutableLiveData<>();

//    private MutableLiveData<Bitmap> imageLiveData = new MutableLiveData<>();

    String detectResult;

    public void detectFaceEmotion(Bitmap bitmap) {
//        Mat mat=new Mat();
//        Utils.bitmapToMat(bitmap,mat);
        try {
            detectResult = null;
            OkHttpClient client = new OkHttpClient();
            // form 表单形式上传
            MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
            requestBody.addFormDataPart("api_key", "six6aKZ9f6pNupm9XZ4HqOsoeSDCRngu");
            requestBody.addFormDataPart("api_secret", "6MrBjWsqZXcwT04L-Z99Iah1zF3qUpLi");
            requestBody.addFormDataPart("return_attributes", "emotion");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            // MediaType.parse() 里面是上传的文件类型。
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), baos.toByteArray());
            // 参数分别为， 请求key ，文件名称 ， RequestBody
            requestBody.addFormDataPart("image_file", "imagefile", body);
            Logger.i(TAG, "开始请求表情识别");
            HttpUtils.sendOkHttpRequest("https://api-cn.faceplusplus.com/facepp/v3/detect", requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.i(TAG, "请求错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String str = response.body().string();
                    Log.i(TAG, "响应：" + str);
                    try {
                        JSONObject jsonObject = new JSONObject(str);
                        JSONObject resObject = jsonObject.getJSONArray("faces").getJSONObject(0);
                        JSONObject emotion = resObject.getJSONObject("attributes").getJSONObject("emotion");
                        JSONObject face_rectangle = resObject.getJSONObject("face_rectangle");
                        double sadness = emotion.getDouble("sadness");
                        double neutral = emotion.getDouble("neutral");
                        double disgust = emotion.getDouble("disgust");
                        double anger = emotion.getDouble("anger");
                        double surprise = emotion.getDouble("surprise");
                        double fear = emotion.getDouble("fear");
                        double happiness = emotion.getDouble("happiness");
                        double ret = Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(sadness, neutral), disgust), anger), surprise), fear), happiness);
                        if (ret == sadness) {
                            detectResult = "难过";
                        } else if (ret == neutral) {
                            detectResult = "平静";
                        } else if (ret == disgust) {
                            detectResult = "厌恶";
                        } else if (ret == anger) {
                            detectResult = "生气";
                        } else if (ret == surprise) {
                            detectResult = "惊讶";
                        } else if (ret == fear) {
                            detectResult = "恐惧";
                        } else if (ret == happiness) {
                            detectResult = "高兴";
                        }
                        sendMessage(Constant.EXPRESSION);
                    } catch (Exception ex) {
                        Log.e("FACE++", "onResponse: ", ex);
                    }
                }
            });
        } catch (Exception ex) {
            Log.e("Face++", "detectFaceEmotionByFaceCPP: ", ex);
        }
    }

    //    public void detectExpression(Bitmap bitmap) {
//        imageLiveData.setValue(bitmap);
//    }
//
//    LiveData<String> expressionLiveData = Transformations.switchMap(imageLiveData,bitmap->{return Repository.getInstance().detectExpression(bitmap);});
    private void sendMessage(int what) {
        Message msg = handler.obtainMessage();
        msg.what = what;
        handler.sendMessage(msg);
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle data;
            switch (msg.what) {
                case Constant.EXPRESSION:
//                    Glide.with(Main3Activity.this).load(face).into(ivFace);
                    expression.setValue(detectResult);
                    break;
                case Constant.LIVE_DETECT:
                    Bundle bundle = msg.getData();
                    faceRectLiveData.setValue(new RelativeRect(bundle.getFloat("left"), bundle.getFloat("top"), bundle.getFloat("right"), bundle.getFloat("bottom")));
            }
        }
    };

    //温度检测

    private MutableLiveData<RelativeRect> faceRectLiveData = new MutableLiveData<>();
//    public LiveData<TemperatureInfo> temperatureInfoLiveData = Transformations.switchMap(faceRectLiveData, faceRect -> Repository.getInstance().liveDetect(faceRect));

    public void liveDetect(float left, float top, float right, float bottom) {
//        faceRectLiveData.setValue(faceRect);
        Bundle bundle = new Bundle();
        bundle.putFloat("left", left);
        bundle.putFloat("top", top);
        bundle.putFloat("right", right);
        bundle.putFloat("bottom", bottom);
        Message message = Message.obtain();
        message.setData(bundle);
        message.what = Constant.LIVE_DETECT;
        handler.sendMessage(message);
    }

}
