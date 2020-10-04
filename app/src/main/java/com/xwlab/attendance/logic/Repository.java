package com.xwlab.attendance.logic;

import android.graphics.Rect;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.xwlab.attendance.AttendanceApplication;
import com.xwlab.attendance.Logger;
import com.xwlab.attendance.Utils;
import com.xwlab.attendance.logic.model.RelativeRect;
import com.xwlab.attendance.logic.model.TemperatureInfo;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Repository {
    //    String detectResult;
    private static Repository instance;

    public synchronized static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }
//    public LiveData<String> detectExpression(Bitmap bitmap) {
//
//        Mat mat=new Mat();
//        Utils.bitmapToMat(bitmap,mat);
//        try {
//            detectResult = null;
//            OkHttpClient client = new OkHttpClient();
//            // form 表单形式上传
//            MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
//            requestBody.addFormDataPart("api_key", "six6aKZ9f6pNupm9XZ4HqOsoeSDCRngu");
//            requestBody.addFormDataPart("api_secret", "6MrBjWsqZXcwT04L-Z99Iah1zF3qUpLi");
//            requestBody.addFormDataPart("return_attributes", "emotion");
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            // MediaType.parse() 里面是上传的文件类型。
//            RequestBody body = RequestBody.create(MediaType.parse("image/*"), baos.toByteArray());
//            // 参数分别为， 请求key ，文件名称 ， RequestBody
//            requestBody.addFormDataPart("image_file", "imagefile", body);
//            Logger.i(TAG, "开始请求表情识别");
//            HttpUtils.sendOkHttpRequest("https://api-cn.faceplusplus.com/facepp/v3/detect", requestBody, new Callback() {
//
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    Logger.i(TAG, "请求错误");
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    String str = response.body().string();
//                    Log.i(TAG, "响应：" + str);
//                    try {
//                        JSONObject jsonObject = new JSONObject(str);
//                        JSONObject resObject = jsonObject.getJSONArray("faces").getJSONObject(0);
//                        JSONObject emotion = resObject.getJSONObject("attributes").getJSONObject("emotion");
//                        JSONObject face_rectangle = resObject.getJSONObject("face_rectangle");
//                        double sadness = emotion.getDouble("sadness");
//                        double neutral = emotion.getDouble("neutral");
//                        double disgust = emotion.getDouble("disgust");
//                        double anger = emotion.getDouble("anger");
//                        double surprise = emotion.getDouble("surprise");
//                        double fear = emotion.getDouble("fear");
//                        double happiness = emotion.getDouble("happiness");
//                        double ret = Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(sadness, neutral), disgust), anger), surprise), fear), happiness);
//                        if (ret == sadness) {
//                            detectResult = "难过";
//                        } else if (ret == neutral) {
//                            detectResult = "平静";
//                        } else if (ret == disgust) {
//                            detectResult = "厌恶";
//                        } else if (ret == anger) {
//                            detectResult = "生气";
//                        } else if (ret == surprise) {
//                            detectResult = "惊讶";
//                        } else if (ret == fear) {
//                            detectResult = "恐惧";
//                        } else if (ret == happiness) {
//                            detectResult = "高兴";
//                        }
//                        sendMessage(Constant.EXPRESSION);
//                        MutableLiveData expressionLiveData = new MutableLiveData<>();
//                        expressionLiveData.setValue(detectResult);
//                        return (LiveData)expressionLiveData;
//                    } catch (Exception ex) {
//                        Log.e("FACE++", "onResponse: ", ex);
//                    }
//                }
//            });
//        } catch (Exception ex) {
//            Log.e("Face++", "detectFaceEmotionByFaceCPP: ", ex);
//        }
//
//        return expressionLiveData;
//    }

    public LiveData<TemperatureInfo> liveDetect(RelativeRect faceRect) {
        float[] mlx90640ImageP0 = new float[768];
        float[] mlx90640ImageP1 = new float[768];
        float[] mlx90640ToP0 = new float[768];
        float[] mlx90640ToP1 = new float[768];

        float[] temperatures = new float[768];
        float[] pixels = new float[768];
        float relaLeft = faceRect.getRelativeLeft();
        float relaTop = faceRect.getRelativeTop();
        float relaRight = faceRect.getRelativeRight();
        float relaBottom = faceRect.getRelativeBottom();
        Logger.i("人脸坐标",faceRect.toString());
        int width = 32;
        int height = 24;
        int thermalLeft = (int) (width * relaLeft);
        int thermalRight = (int) (width * relaRight);
        int thermalTop = (int) (height * relaTop);
        int thermalBottom = (int) (height * relaBottom);
        int w = thermalRight - thermalLeft;
        int h = thermalBottom - thermalTop;

        int faceArea = w * h;
        int backgroundArea = (thermalBottom + 1) * width - faceArea;
        int faceCount = 0;
        int backgroundCount = 0;


        int ret = AttendanceApplication.MLX90640.MLX90640_Measure(mlx90640ImageP0, mlx90640ImageP1, mlx90640ToP0, mlx90640ToP1);
//        Logger.i(TAG, "温度图"+Arrays.toString(mTemperature));
//        float[] max5 = new float[]{0, 0};
//        float min = 0;
//        int index = 0;
        if (ret == 0) {
            float faceTemperature = 0L;
            for (int i = 0; i < 768; ++i) {
                temperatures[i] = mlx90640ToP0[i] + mlx90640ToP1[i];
                pixels[i] = mlx90640ImageP0[i] + mlx90640ImageP1[i];


                if (i <= (thermalBottom + 1) * width) { //脖子以上区域
                    if ((i > (thermalTop + 1) * width) && ((i - thermalLeft) % width >= 0) && (i - thermalLeft) % width < (thermalRight - thermalLeft)) {    //脸部区域
//                    mTemperature[i] = 50;
                        if (faceTemperature < temperatures[i]) {
                            faceTemperature = temperatures[i];
                        }
//                    if (mTemperature[i] > min) {    //求最大的五个温度值
//                        max5[index] = mTemperature[i];
//                        min = mTemperature[i];
//                        for (int j = 0; j < 2; j++) {
//                            if (min > max5[j]) {
//                                min = max5[j];
//                                index = j;
//                            }
//                        }
//                    }
                        if (temperatures[i] >= 32 && temperatures[i] <= 41) {
                            faceCount++;
                        }

                    } else if (temperatures[i] >= 30) {
                        backgroundCount++;
                    }
                }

                if ((i >= thermalTop * width) && (i < (thermalBottom + 1) * width) &&
                        (((i > thermalTop * width + thermalLeft) && (i <= thermalTop * width + thermalRight)) ||
                                ((i > thermalBottom * width + thermalLeft) && (i <= thermalBottom * width + thermalRight)) ||
                                ((i - thermalLeft) % width == 0) || ((i - thermalRight) % width == 0))) {
                    temperatures[i] = 20;
                }
            }
//        float sum = 0;
//        for (int j = 0; j < 2; j++) {
//            sum += max5[j];
//        }
//        faceTemperature = sum/2;
//        System.out.println(Arrays.toString(mTemperature));
//        System.out.println(Arrays.toString(mImages));
//        if (ret == 0) {
//            sendMessage(Constant.THERMAL);
//            mTvTemperature.setText("当前温度：" + Utils.t1f(max) + "度");
//            mGridView.setTemperature(mTemperature, mImages);
//        } else {
//            mTvTemperature.setText("MLX90640数据读取错误");
//            Toast.makeText(this, "MLX90640数据读取错误", Toast.LENGTH_LONG).show();
//        }
            float f = (float) faceCount / faceArea;
            float b = (float) backgroundCount / backgroundArea;
            Logger.i(TAG, "人脸：" + faceArea + "背景：" + backgroundArea + "人脸正常像素：" + faceCount + "比例：" + f + "背景异常像素：" + backgroundCount + "比例：" + b);
            boolean isRealFace = (float) faceCount / faceArea > 0.4;
            TemperatureInfo temperatureInfo = new TemperatureInfo(temperatures, pixels, faceTemperature, isRealFace);
            return new MutableLiveData<>(temperatureInfo);
        } else {
            Utils.showToast("热成像模块数据读取错误");
            return null;
        }
    }
}
