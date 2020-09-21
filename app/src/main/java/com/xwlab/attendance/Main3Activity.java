package com.xwlab.attendance;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.lztek.tools.irmeter.MLX906xx;
import com.xwlab.attendance.ui.DetectedViewModel;
import com.xwlab.expression.ExpresionRegcognition;
import com.xwlab.util.CodeHints;
import com.xwlab.util.Constant;
import com.xwlab.widget.Camera2View;
import com.xwlab.widget.FaceView;
import com.xwlab.widget.MLXGridView;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

import static com.xwlab.attendance.HttpUtils.sendJsonPost;

//import android.support.v7.app.AppCompatActivity;

public class Main3Activity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "WorkActivity";
    private Face mFace = new Face();
    private FaceDatabase mFaceDatabase;
    private boolean workThreadFlag = true;
    private static File sdDir = Environment.getExternalStorageDirectory();
    private static final String sdPath = sdDir.toString() + "/attendance/";

    private Camera2View cvPreview;
    private ImageView ivFace, ivBackground;
    private TextView tvResult, etPwd;
    private FaceView fvFace;

    private String password = "", showText = "";
    private boolean encryption = true;     //1表示开启加密模式
    private Bitmap face;
    private Uri ringtoneUri = Uri.parse("android.resource://" + R.raw.beep);
    private Ringtone ringtone;

    protected Handler mHandler;

    protected MLX906xx mMLX90640;
    protected TextView mTvTemperature;
    protected MLXGridView mGridView;

//    protected Spinner mSpnRefreshRate;

    Button btnEncrypt;
    private DetectedViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        UIInit();
        //初始化人脸识别模型、更新加载数据库
        mFace.FaceModelInit(sdPath);
        mFaceDatabase = new FaceDatabase(getApplicationContext());
        mFaceDatabase.updateDatabase();

        mHandler = new Handler();
        mGridView.setModuleType(MLXGridView.MLX90640);
        mMLX90640 = new MLX906xx();

        if (!OpenCVLoader.initDebug()) {
            Log.d("opencv", "初始化失败");
        }

        viewModel = new ViewModelProvider(this).get(DetectedViewModel.class);
        viewModel.expression.observe(this, expression -> tvExpression.setText(expression));
    }

    @Override
    protected void onStart() {
        super.onStart();
        workThreadFlag = true;
        new Thread(new FDThread()).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        workThreadFlag = false;
    }

    private Runnable mAutoRefreshRunnable = null;

//    private void onBtnAuto() {
//        if (null == mAutoRefreshRunnable) {
//            if (mlx90640InitializeCheck() < 0) {
//                return;
//            }
//            mAutoRefreshRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    mlx90640Measure();
//                    int interval = 500;
//                    int refreshRate = mMLX90640.getRefreshRate();
//                    if (refreshRate != 0) {
//                        interval = ((1000 * 1000) >> (refreshRate - 1)) / 1000;
//                    } else {
//                        interval = 2000;
//                    }
//                    mHandler.removeCallbacks(this);
//                    mHandler.postDelayed(this, interval < 100 ? 500 : interval);
//                }
//            };
//            mHandler.postDelayed(mAutoRefreshRunnable, 100);
//        } else {
//            mHandler.removeCallbacks(mAutoRefreshRunnable);
//            mAutoRefreshRunnable = null;
//        }
//    }

    private int[] mRefreshRateValues = new int[]{
            MLX906xx.MLX90640Refresh1HZ,
            MLX906xx.MLX90640Refresh2HZ,
            MLX906xx.MLX90640Refresh4HZ,
            MLX906xx.MLX90640Refresh8HZ,
            MLX906xx.MLX90640Refresh16HZ,
    };

    protected int mlx90640InitializeCheck() {

        int refreshRate = mRefreshRateValues[4];

        if (refreshRate == mMLX90640.getRefreshRate()) {
            return refreshRate;
        }

        int ret = mMLX90640.MLX90640_InitProcedure(refreshRate);
        if (0 != ret) {
            Toast.makeText(this, "MLX90640初始化失败", Toast.LENGTH_LONG).show();
//            mTvTemperature.setText("MLX90640初始化失败！");
            return ret;
        } else {
//            mTvTemperature.setText("MLX90640初始化成功!");
            return mMLX90640.getRefreshRate();
        }
    }


    float[] mlx90640ImageP0 = new float[768];
    float[] mlx90640ImageP1 = new float[768];
    float[] mlx90640ToP0 = new float[768];
    float[] mlx90640ToP1 = new float[768];

    float[] mTemperature = new float[768];
    float[] mImages = new float[768];

    protected void mlx90640Measure() {
        int ret = mMLX90640.MLX90640_Measure(mlx90640ImageP0, mlx90640ImageP1, mlx90640ToP0, mlx90640ToP1);

        float max = Float.MIN_VALUE;
        for (int i = 0; i < 768; ++i) {
            mTemperature[i] = mlx90640ToP0[i] + mlx90640ToP1[i];
            mImages[i] = mlx90640ImageP0[i] + mlx90640ImageP1[i];
            if (max < mTemperature[i]) {
                max = mTemperature[i];
            }
        }
        System.out.println(Arrays.toString(mTemperature));
        System.out.println(Arrays.toString(mImages));
        if (ret == 0) {
            mTvTemperature.setText("当前温度：" + Utils.t1f(max) + "度");
            mGridView.setTemperature(mTemperature, mImages);
        } else {
            mTvTemperature.setText("MLX90640数据读取错误");
            Toast.makeText(this, "MLX90640数据读取错误", Toast.LENGTH_LONG).show();
        }
    }

    private TextView tvExpression;

    private void UIInit() {
        mTvTemperature = findViewById(R.id.tv_temperature);
        mGridView = findViewById(R.id.grid24x32view);
        cvPreview = findViewById(R.id.cv_preview);
        tvExpression = findViewById(R.id.tv_expression);
//        ivFace = findViewById(R.id.iv_face);
        fvFace = findViewById(R.id.fv_face);
        tvResult = findViewById(R.id.tv_result);
        etPwd = findViewById(R.id.et_pwd);
        btnEncrypt = findViewById(R.id.btn_encrypt);
        ivBackground = findViewById(R.id.iv_background);
        Glide.with(this).load(R.drawable.access_background).into(ivBackground);

        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
        findViewById(R.id.button7).setOnClickListener(this);
        findViewById(R.id.button8).setOnClickListener(this);
        findViewById(R.id.button9).setOnClickListener(this);
        findViewById(R.id.button0).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.btn_encrypt).setOnClickListener(this);
        findViewById(R.id.btn_updateSQL).setOnClickListener(this);
//        cvPreview.open(CameraCharacteristics.LENS_FACING_BACK);//平板
        cvPreview.open(CameraCharacteristics.LENS_FACING_FRONT);//3288
//        cvPreview.open(CameraCharacteristics.LENS_FACING_EXTERNAL);//3288
    }


    @Override
    public void onClick(View view) {
        int resId = view.getId(); // 获得当前按钮的编号
        switch (resId) {
            case R.id.btn_encrypt:
                if (encryption == false) {
                    btnEncrypt.setText("关闭加密模式");
                    encryption = true;
//                cvPreview.setVisibility(View.INVISIBLE);
                } else {
                    btnEncrypt.setText("开启加密模式");
                    encryption = false;
//                cvPreview.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.button_cancel:
                tvResult.setText("");
                if (password.length() > 0) {
                    password = password.substring(0, password.length() - 1);
                    showText = showText.substring(0, showText.length() - 1);
                }
                break;
            case R.id.btn_updateSQL:
                mFaceDatabase.updateDatabase();
                break;
            default:
                if (password.length() < 6) {
                    String inputText = ((TextView) view).getText().toString();
                    password = password + inputText;
                    showText = showText + '·';
                }
                if (password.length() == 6) {
                    if (mFaceDatabase.passwordCmp(password)) {
                        tvResult.setText("验证成功");
                    } else {
                        tvResult.setText("密码输入错误");
                    }
                    sendMessageDelayed(Constant.CLEAN_TEXT, 2000);
                    showText = "";
                    password = "";
                } else {
                    tvResult.setText("");
                }
                break;
        }
        etPwd.setText(showText);
    }

    private class FDThread implements Runnable {
        private String name, phoneNum;
        private Long lastTime, expressionTime = Long.valueOf(0), startTime, temperatureTime = Long.valueOf(0);
        private String QRCode, lastFacePhoneNum, lastQRCPhoneNum;
        Bitmap image, faceRect;
        ExpresionRegcognition expresionRegcognition = new ExpresionRegcognition();
        boolean trueFace = false;

        @Override
        public void run() {
            Logger.i(TAG, "start FDThread");
            while (!Thread.currentThread().isInterrupted() && workThreadFlag) {
                startTime = System.currentTimeMillis();
                image = cvPreview.getBitmap();
                if (image == null) {
                    continue;
                }
                int width = image.getWidth();
                int height = image.getHeight();
                //镜像水平翻转
                Matrix m = new Matrix();
                m.postScale(-1, 1);
                image = Bitmap.createBitmap(image, 0, 0, width, height, m, true);
                Logger.i(TAG, "取画面并翻转：" + (System.currentTimeMillis() - startTime));

                startTime = System.currentTimeMillis();
                byte[] imageData = getPixelsRGBA(image);    //bitmap转字节数组
                int[] faceInfo = mFace.FaceDetect(imageData, width, height, 4);
                Logger.i(TAG, "人脸检测：" + (System.currentTimeMillis() - startTime));

                startTime = System.currentTimeMillis();
                if (faceInfo[0] > 0) {     //画面中有人脸
                    Logger.i(TAG, "检测到人脸");
                    //表情识别
                    long sysTime = new Date().getTime();
                    if (System.currentTimeMillis() - expressionTime > 2000) {
                        expressionTime = System.currentTimeMillis();
                        viewModel.detectFaceEmotion(image);
                    }
                    Logger.i(TAG, "表情识别：" + (System.currentTimeMillis() - startTime));

                    startTime = System.currentTimeMillis();
                    //截取人脸
                    int left = faceInfo[1];
                    int top = faceInfo[2];
                    int right = faceInfo[3];
                    int bottom = faceInfo[4];
                    Rect rect = new Rect(left, top, right, bottom);
                    //温度检测

                    if (System.currentTimeMillis() - temperatureTime > 1000) {
                        temperatureTime = System.currentTimeMillis();
                        trueFace = liveDetect((float) left / width, (float) right / width, (float) top / height, (float) bottom / height);
                        Logger.i(TAG, "温度检测：" + (System.currentTimeMillis() - startTime));
                    }

                    startTime = System.currentTimeMillis();
                    //加密或者画矩形框
                    faceRect = Bitmap.createBitmap(image, left, top, right - left, bottom - top);
                    if (encryption == true) {
                        fvFace.encryptFace(faceRect, rect);
                    } else {
                        fvFace.drawRect(rect);
                    }
                    Logger.i(TAG, "画框或加密：" + (System.currentTimeMillis() - startTime));

                    startTime = System.currentTimeMillis();
                    //特征提取
                    byte[] faceDate = getPixelsRGBA(faceRect);
                    String feature = mFace.FaceFeatureRestore(faceDate, faceRect.getWidth(), faceRect.getHeight());
                    Logger.i(TAG, "特征提取：" + (System.currentTimeMillis() - startTime));

                    startTime = System.currentTimeMillis();
                    Object[] objects = mFaceDatabase.featureCmp(feature);
                    name = (String) objects[0];
                    phoneNum = (String) objects[1];
                    if (!TextUtils.isEmpty(phoneNum)) {     //匹配成功
                        if (trueFace) {
                            welcome(name);
                            sendMessageDelayed(Constant.CLEAN_TEXT, 2000);
                            long time = System.currentTimeMillis();
                            if (phoneNum.equals(lastFacePhoneNum)) {        //与上一位识别的人相同
                                long interval = System.currentTimeMillis() - time;
                                if (interval > 5000) {
                                    new Thread(new FDHttpThread(phoneNum, name)).start();
                                }
                            } else {            //与上一位识别的人不相同
                                lastFacePhoneNum = phoneNum;
                                new Thread(new FDHttpThread(phoneNum, name)).start();
                            }
                        } else {
                            sendMessage(Constant.FAKE_FACE);
                            sendMessageDelayed(Constant.CLEAN_TEXT, 2000);
                        }
                        Logger.i(TAG, "特征匹配：" + (System.currentTimeMillis() - startTime));
                    }
//                    if (name.equals("unknown")) {
//                        Logger.i(TAG, "unknown");
//                        if (!TextUtils.isEmpty(lastUnknownFeature)) {
//                            double sim = mFaceDatabase.calculSimilar(feature, lastUnknownFeature);
////                            Logger.i(TAG, "sim:" + sim);
//                            if (sim > 0.6) {
//                                Long time = sysTime - lastUnknownTime;
//                                if (time > 60000) {
//                                    lastUnknownTime = sysTime;
//                                    sendMessage(Constant.UNKNOWN);
//                                    HttpService.reportUnknown(face, encryption);
//                                }
//                            } else {
//                                lastUnknownFeature = feature;
//                                lastUnknownTime = sysTime;
//                                sendMessage(Constant.UNKNOWN);
//                                HttpService.reportUnknown(face, encryption);
//                            }
//                        } else {
//                            lastUnknownFeature = feature;
//                            lastUnknownTime = sysTime;
//                            sendMessage(Constant.UNKNOWN);
//                            HttpService.reportUnknown(face, encryption);
//                        }
//                        haveFace = false;
//                    } else if (name.equals("")) {
//                        haveFace = false;
//                    } else if (name.equals(lastName)) {
//                        long time = sysTime - lastTime;
//                        haveFace = time > 1000;
//                        if (haveFace) {
//                            new Thread(new FDHttpThread(phoneNum, name)).start();
//                            lastTime = sysTime;
//                        }
//                    } else {
//                        haveFace = true;
//                        lastName = name;
//                        lastTime = sysTime;
//                        new Thread(new FDHttpThread(phoneNum, name)).start();
//                    }
                } else {
                    fvFace.clearCanvas();
//                    haveFace = false;
//                    sendMessage(Constant.CLOSE_FACE);

                    Result result = decodeQR(image);
                    if (result != null) {   //有二维码
                        String[] data = result.toString().split("-");
                        if (data.length == 3 && data[0].equals("blueCity")) {   //是目标二维码
                            phoneNum = data[1];
                            QRCode = data[2];
//                        Logger.e(TAG,result.toString());
                            Long sysTime = new Date().getTime();
//                        Logger.i(TAG, sysTime.toString());
                            if (phoneNum.equals(lastQRCPhoneNum)) {     //与上一个二维码相同
                                long interval = sysTime - lastTime;
//                            Logger.i(TAG, time.toString());
                                if (interval > 2000) {     //间隔大于2秒
                                    if (ringtone != null) {
                                        // 停止播放铃声
                                        ringtone.stop();
                                    }
                                    ringtone = RingtoneManager.getRingtone(Main3Activity.this, ringtoneUri);
                                    ringtone.play();
                                    new Thread(new QRCodeHttpThread(phoneNum, QRCode)).start();
                                }
                            } else {        //与上一个二维码不同
                                if (ringtone != null) {
                                    // 停止播放铃声
                                    ringtone.stop();
                                }
                                ringtone = RingtoneManager.getRingtone(Main3Activity.this, ringtoneUri);
                                ringtone.play();
                                new Thread(new QRCodeHttpThread(phoneNum, QRCode)).start();
                            }
                            lastTime = sysTime;
                            lastQRCPhoneNum = phoneNum;
                        }
                    }
                }
            }
        }
    }

    /**
     * 活体检测
     */
    private boolean liveDetect(float relaLeft, float relaRight, float relaTop, float relaBottom) {
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

        if (mlx90640InitializeCheck() < 0) {
            return false;
        }
        int ret = mMLX90640.MLX90640_Measure(mlx90640ImageP0, mlx90640ImageP1, mlx90640ToP0, mlx90640ToP1);
//        Logger.i(TAG, "温度图"+Arrays.toString(mTemperature));
        float[] max5 = new float[]{0, 0};
        float min = 0;
        int index = 0;
        for (int i = 0; i < 768; ++i) {
            mTemperature[i] = mlx90640ToP0[i] + mlx90640ToP1[i];
            mImages[i] = mlx90640ImageP0[i] + mlx90640ImageP1[i];
//            if (max < mTemperature[i]) {
//                max = mTemperature[i];
//            }


            if (i <= (thermalBottom + 1) * width) { //脖子以上区域
                if ((i > (thermalTop + 1) * width) && ((i - thermalLeft) % width >= 0) && (i - thermalRight) % width < (thermalRight - thermalLeft)) {    //脸部区域
                    if (mTemperature[i] > min) {    //求最大的五个温度值
                        max5[index] = mTemperature[i];
                        min = mTemperature[i];
                        for (int j = 0; j < 2; j++) {
                            if (min > max5[j]) {
                                min = max5[j];
                                index = j;
                            }
                        }
                    }
                    if (mTemperature[i] >= 26 && mTemperature[i] <= 41) {
                        faceCount++;
                    }

                } else if (mTemperature[i] >= 30) {
                    backgroundCount++;
                }
            }

            if ((i >= thermalTop * width) && (i < (thermalBottom + 1) * width) &&
                    (((i > thermalTop * width + thermalLeft) && (i <= thermalTop * width + thermalRight)) ||
                            ((i > thermalBottom * width + thermalLeft) && (i <= thermalBottom * width + thermalRight)) ||
                            ((i - thermalLeft) % width == 0) || ((i - thermalRight) % width == 0))) {
                mTemperature[i] = 50;
            }
        }
        float sum = 0;
        for (int j = 0; j < 2; j++) {
            sum += max5[j];
        }
        faceTemperature = sum/2;
        System.out.println(Arrays.toString(mTemperature));
        System.out.println(Arrays.toString(mImages));
        if (ret == 0) {
            sendMessage(Constant.THERMAL);
//            mTvTemperature.setText("当前温度：" + Utils.t1f(max) + "度");
//            mGridView.setTemperature(mTemperature, mImages);
        } else {
//            mTvTemperature.setText("MLX90640数据读取错误");
            Toast.makeText(this, "MLX90640数据读取错误", Toast.LENGTH_LONG).show();
        }
        float f = (float) faceCount / faceArea;
        float b = (float) backgroundCount / backgroundArea;
        Logger.i(TAG, "人脸：" + faceArea + "背景：" + backgroundArea + "人脸正常像素：" + faceCount + "比例：" + f + "背景异常像素：" + backgroundCount + "比例：" + b);
        return (float) faceCount / faceArea > 0.6;
    }

    /*
    人脸匹配成功，添加记录
     */
    private class FDHttpThread implements Runnable {
        String name, phoneNum;

        FDHttpThread(String phoneNum, String name) {
            this.phoneNum = phoneNum;
            this.name = name;
        }

        @Override
        public void run() {

            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put("service", "door.attendance.enter");
                requestBody.put("phoneNum", phoneNum);
                requestBody.put("name", name);
                requestBody.put("community", "凤凰城");
                requestBody.put("building", "05");
                requestBody.put("method", "face");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String response = sendJsonPost(requestBody.toString());
            try {
                JSONObject result = new JSONObject(response);
                if (result.optInt("resultCode") == -1) {    //二维码验证通过
                    Logger.i(TAG, "人脸验证通过，已添加记录");
                } else {
                    Logger.i(TAG, "人脸验证通过，未添加记录");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class QRCodeHttpThread implements Runnable {
        String name, phoneNum, QRCode;

        QRCodeHttpThread(String phoneNum, String QRCode) {
            this.phoneNum = phoneNum;
            this.QRCode = QRCode;
        }

        @Override
        public void run() {
            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put("service", "door.attendance.QRCode");
                requestBody.put("phoneNum", phoneNum);
                requestBody.put("community", "凤凰城");
                requestBody.put("building", "05");
                requestBody.put("QRCode", QRCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String response = sendJsonPost(requestBody.toString());
            try {
                JSONObject result = new JSONObject(response);
                if (result.optInt("resultCode") == -1) {    //二维码验证通过
                    name = result.optString("personName");
                    welcome(name);
                    sendMessageDelayed(Constant.CLEAN_TEXT, 2000);
//                    sleep(2000);
//                    sendMessage(Constant.CLEAN_TEXT);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解码二维码
     */
    private Result decodeQR(Bitmap srcBitmap) {
        Result result = null;
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        int[] pixels = new int[width * height];
        srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            result = reader.decode(binaryBitmap, CodeHints.getDefaultDecodeHints());
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void sendMessage(int what) {
        Message msg = showHandler.obtainMessage();
        msg.what = what;
        showHandler.sendMessage(msg);
    }

    private void sendMessageDelayed(int what, long delayMillis) {
        Message msg = showHandler.obtainMessage();
        msg.what = what;
        showHandler.sendMessageDelayed(msg, delayMillis);
    }

    private void welcome(String name) {
        showHandler.removeCallbacksAndMessages(null);
        Message msg = showHandler.obtainMessage();
        msg.what = Constant.WELCOME;
        Bundle data = new Bundle();
        data.putString("name", name);
        msg.setData(data);
        showHandler.sendMessage(msg);
    }

    float faceTemperature;
    private Handler showHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle data;
            switch (msg.what) {
                case Constant.UPDATE_FACE:
//                    Glide.with(Main3Activity.this).load(face).into(ivFace);
                    ivFace.setImageBitmap(face);
                    break;
                case Constant.CLOSE_FACE:
                    ivFace.setImageBitmap(null);
//                    tvResult.setText("");
                    break;
                case Constant.UNKNOWN:
                    data = msg.getData();
                    tvResult.setText("未知人员");
                    break;
                case Constant.WELCOME:
                    data = msg.getData();
                    String name = data.getString("name");
                    tvResult.setText("您好，" + name);
                    break;
                case Constant.CLEAN_TEXT:
                    tvResult.setText("");
                    break;
                case Constant.VERIFY_SUCCESSFULLY:
                    tvResult.setText("验证通过");
                    break;
                case Constant.FAKE_FACE:
                    tvResult.setText("疑似假脸");
                    break;
                case Constant.THERMAL:
                    mTvTemperature.setText("当前温度：" + Utils.t1f(faceTemperature) + "度");
                    mGridView.setTemperature(mTemperature, mImages);
                    break;
            }
        }
    };

    private byte[] getPixelsRGBA(Bitmap image) {
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        image.copyPixelsToBuffer(buffer);
        return buffer.array();
    }

    /**
     * 人脸马赛克化
     */
    private Bitmap encryptBitmap(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] pix = new int[w * h];
        src.getPixels(pix, 0, w, 0, 0, w, h);
        int[] newPix = new int[w * h];
        int blockSize = 20;
        for (int y = 0; y < h / blockSize; y++) {
            for (int x = 0; x < w / blockSize; x++) {
                for (int j = 0; j < blockSize / 2; j++) {
                    for (int i = 0; i < blockSize; i++) {
                        if (i < blockSize / 2) {
                            int index1 = (y * blockSize + j) * w + i + x * blockSize;
                            int index2 = index1 + (w + 1) * blockSize / 2;
                            newPix[index1] = pix[index2];
                            newPix[index2] = pix[index1];
                        } else {
                            int index1 = (y * blockSize + j) * w + i + x * blockSize;
                            int index2 = index1 + (w - 1) * blockSize / 2;
                            newPix[index1] = pix[index2];
                            newPix[index2] = pix[index1];
                        }
                    }
                }
            }
        }
        Bitmap res = Bitmap.createBitmap(w, h, src.getConfig());
        res.setPixels(newPix, 0, w, 0, 0, w, h);
        return res;
    }
}