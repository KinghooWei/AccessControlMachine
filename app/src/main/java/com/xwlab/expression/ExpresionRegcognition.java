package com.xwlab.expression;

import android.graphics.Bitmap;
import android.util.Log;

import com.xwlab.attendance.HttpUtils;

import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExpresionRegcognition {
    public String detectFaceEmotion(Bitmap bitmap){
        try{
            return detectFaceEmotionByFaceCPP(bitmap);
//            if(selectedApi.equals("Face++")){
//                return  detectFaceEmotionByFaceCPP(bitmap);
//            }else{
//                Rect[] facesArray=detectFaceRectByMTCNN(bitmap);
//                String ret =  detectFaceEmotion(bitmap,facesArray);
//                Mat mat=new Mat();
//                Utils.bitmapToMat(bitmap,mat);
//                Imgproc.putText(mat, "Inner", new Point(5, 40), 3, 1, new Scalar(0, 255, 0, 255), 2);
//                Utils.matToBitmap(mat,bitmap);
//                return  ret;
//            }
        }catch (Exception ex){
            Log.e("facedetect","detectFaceEmotion:",ex);
        }
        return null;
    }
    private  String detectResult=null;
    public String detectFaceEmotionByFaceCPP(Bitmap bitmap){
        Mat mat=new Mat();
        Utils.bitmapToMat(bitmap,mat);
        try{
            detectResult=null;
            OkHttpClient client = new OkHttpClient();
            // form 表单形式上传
            MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
            requestBody.addFormDataPart("api_key","six6aKZ9f6pNupm9XZ4HqOsoeSDCRngu");
            requestBody.addFormDataPart("api_secret","6MrBjWsqZXcwT04L-Z99Iah1zF3qUpLi");
            requestBody.addFormDataPart("return_attributes","emotion");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            // MediaType.parse() 里面是上传的文件类型。
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), baos.toByteArray());
            // 参数分别为， 请求key ，文件名称 ， RequestBody
            requestBody.addFormDataPart("image_file", "imagefile", body);
            HttpUtils.sendOkHttpRequest("https://api-cn.faceplusplus.com/facepp/v3/detect", requestBody, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String str = response.body().string();
                    Log.i("FACE++", response.message() + " , body " + str);
                    try {
                        JSONObject jsonObject=new JSONObject(str);
                        JSONObject resObject = jsonObject.getJSONArray("faces").getJSONObject(0);
                        JSONObject emotion = resObject.getJSONObject("attributes").getJSONObject("emotion");
                        JSONObject face_rectangle = resObject.getJSONObject("face_rectangle");
                        double sadness=emotion.getDouble("sadness");
                        double neutral=emotion.getDouble("neutral");
                        double disgust=emotion.getDouble("disgust");
                        double anger=emotion.getDouble("anger");
                        double surprise=emotion.getDouble("surprise");
                        double fear=emotion.getDouble("fear");
                        double happiness=emotion.getDouble("happiness");
                        double ret = Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(sadness,neutral),disgust),anger),surprise),fear),happiness);
                        if ( ret == sadness){
                            detectResult="难过";
                        }else if (ret==neutral){
                            detectResult="一般";
                        }
                        else if (ret==disgust){
                            detectResult="厌恶";
                        }
                        else if (ret==anger){
                            detectResult="生气";
                        }
                        else if (ret==surprise){
                            detectResult="惊讶";
                        }
                        else if (ret==surprise){
                            detectResult="恐惧";
                        }
                        else if (ret==happiness){
                            detectResult="高兴";
                        }
                        double left=face_rectangle.getDouble("left");
                        double top=face_rectangle.getDouble("top");
                        double width=face_rectangle.getDouble("width");
                        double height=face_rectangle.getDouble("height");
                        Imgproc.rectangle(mat,new Point(left,top),new Point(left+width,top+height),new Scalar(0, 255, 0, 255), 3);
                    }catch (Exception ex){
                        Log.e("FACE++", "onResponse: ",ex );
                    }
                }
            });
//            Request request = new Request.Builder().url("https://api-cn.faceplusplus.com/facepp/v3/detect").post(requestBody.build()).tag(null).build();
//            // readTimeout("请求超时时间" , 时间单位);
//            Call call = client.newBuilder().readTimeout(8000, TimeUnit.MILLISECONDS).build().newCall(request);
//            try {
//                Response response=call.execute();
//                if (response.isSuccessful()){
//                    String str = response.body().string();
//                    Log.i("FACE++", response.message() + " , body " + str);
//                    /*
//                    {
//                      "image_id": "B7B3u1yn5FThJtP2uDnJtQ==",
//                      "request_id": "1542190044,47db8847-263b-42fe-8e26-0c4f646b2446",
//                      "time_used": 228,
//                      "faces": [
//                        {
//                          "attributes": {
//                            "emotion": {
//                              "sadness": 0.07,
//                              "neutral": 6.487,
//                              "disgust": 0.452,
//                              "anger": 0.034,
//                              "surprise": 0.059,
//                              "fear": 15.858,
//                              "happiness": 77.04
//                            }
//                          },
//                          "face_rectangle": {
//                            "width": 58,
//                            "top": 49,
//                            "left": 54,
//                            "height": 58
//                          },
//                          "face_token": "4733a62474eeffecc9e254edbca97661"
//                        }
//                      ]
//                    }
//                     */
//                    try {
//                        JSONObject jsonObject=new JSONObject(str);
//                        JSONObject resObject = jsonObject.getJSONArray("faces").getJSONObject(0);
//                        JSONObject emotion = resObject.getJSONObject("attributes").getJSONObject("emotion");
//                        JSONObject face_rectangle = resObject.getJSONObject("face_rectangle");
//                        double sadness=emotion.getDouble("sadness");
//                        double neutral=emotion.getDouble("neutral");
//                        double disgust=emotion.getDouble("disgust");
//                        double anger=emotion.getDouble("anger");
//                        double surprise=emotion.getDouble("surprise");
//                        double fear=emotion.getDouble("fear");
//                        double happiness=emotion.getDouble("happiness");
//                        double ret = Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(sadness,neutral),disgust),anger),surprise),fear),happiness);
//                        if ( ret == sadness){
//                            detectResult="难过";
//                        }else if (ret==neutral){
//                            detectResult="一般";
//                        }
//                        else if (ret==disgust){
//                            detectResult="厌恶";
//                        }
//                        else if (ret==anger){
//                            detectResult="生气";
//                        }
//                        else if (ret==surprise){
//                            detectResult="惊讶";
//                        }
//                        else if (ret==surprise){
//                            detectResult="恐惧";
//                        }
//                        else if (ret==happiness){
//                            detectResult="高兴";
//                        }
//                        double left=face_rectangle.getDouble("left");
//                        double top=face_rectangle.getDouble("top");
//                        double width=face_rectangle.getDouble("width");
//                        double height=face_rectangle.getDouble("height");
//                        Imgproc.rectangle(mat,new Point(left,top),new Point(left+width,top+height),new Scalar(0, 255, 0, 255), 3);
//                    }catch (Exception ex){
//                        Log.e("FACE++", "onResponse: ",ex );
//                    }
//                }

//            } catch (IOException e) {
//                Log.e("Face++", "detectFaceEmotionByFaceCPP json: ", e);
//            }
        }catch (Exception ex){
            Log.e("Face++", "detectFaceEmotionByFaceCPP: ", ex);
        }
        //Imgproc.putText(mat, "Face++", new Point(5, 40), 3, 1, new Scalar(0, 255, 0, 255), 2);
        Utils.matToBitmap(mat,bitmap);
        return detectResult;
    }
}
