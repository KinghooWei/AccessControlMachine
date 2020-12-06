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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.lztek.tools.irmeter.MLX906xx;
import com.serialport.yzrfidAPI;
import com.xwlab.attendance.logic.model.EncryptFace;
import com.xwlab.attendance.ui.DetectedViewModel;
import com.xwlab.expression.ExpresionRegcognition;
import com.xwlab.util.CodeHints;
import com.xwlab.util.Constant;
import com.xwlab.util.SharedPreferencesUtil;
import com.xwlab.widget.Camera2View;
import com.xwlab.widget.FaceView;
import com.xwlab.widget.MLXGridView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;

import static com.xwlab.attendance.HttpUtils.sendJsonPost;

public class Main3Activity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "WorkActivity";
    private Face mFace = new Face();
    private FaceDatabase mFaceDatabase;
    private boolean workThreadFlag;
    private boolean hasFaceChange=true;         //有无人脸切换
    private static File sdDir = Environment.getExternalStorageDirectory();
    private static final String sdPath = sdDir.toString() + "/attendance/";

    private Camera2View cvPreview;
    private TextView tvResult, etPwd;
    private FaceView fvFace;
    protected TextView tvTemperature;
    protected MLXGridView mGridView;
    private FloatingActionButton fabSwitch;

    String community;
    String gate;
    String longitude;
    String latitude;

    private String password = "", showText = "";
    private boolean encryption = false;     //1表示开启加密模式
    private Uri ringtoneUri = Uri.parse("android.resource://" + R.raw.beep);
    private Ringtone ringtone;


    Button btnEncrypt;
    private DetectedViewModel viewModel;

    Handler handler;
    //    Handler mHandler;
    public yzrfidAPI myzrfidAPI = new yzrfidAPI();
    static int nfd = 0;
    static int nIsOpenCom = 0;
    int nStatus = 0;

    private MLX906xx mMLX90640;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        setSupportActionBar(findViewById(R.id.toolbar));
        viewModel = new ViewModelProvider(this).get(DetectedViewModel.class);
        UIInit();
        //初始化人脸识别模型、更新加载数据库
        mFace.FaceModelInit(sdPath);
        mFaceDatabase = new FaceDatabase(getApplicationContext());
        mFaceDatabase.updateDatabase();

        //测温模块初始化
        mGridView.setModuleType(MLXGridView.MLX90640);
        mMLX90640 = new MLX906xx();
//        mlx90640InitializeCheck();

        //刷卡模块初始化
        handler = new Handler();
        rfInitComFun();
        if (nIsOpenCom != 0) {
            findCard();
        }

//        mHandler = new Handler();
//        onBtnAuto();
//        if (!OpenCVLoader.initDebug()) {
//            Log.d("opencv", "初始化失败");
//        }

//                cvPreview.open(CameraCharacteristics.LENS_FACING_BACK);//平板
        cvPreview.open(CameraCharacteristics.LENS_FACING_FRONT);//3288
//        cvPreview.open(CameraCharacteristics.LENS_FACING_EXTERNAL);//3288
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
        rfClosePortFun();
    }

//    private Runnable mAutoRefreshRunnable = null;

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.function, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.downloadDb:
                tvResult.setText("正在更新数据库...");
                mFaceDatabase.updateDatabase();
                tvResult.setText("数据库更新完毕");
                sendMessageDelayed(Constant.CLEAN_TEXT, 1000);
                break;
            case R.id.encrypt:
                if (!encryption) {
                    item.setIcon(R.drawable.ic_jiami);
                    encryption = true;
//                cvPreview.setVisibility(View.INVISIBLE);
                } else {
                    item.setIcon(R.drawable.ic_bujiami);
                    encryption = false;
//                cvPreview.setVisibility(View.VISIBLE);
                }
                break;
        }
        return true;
    }

    private int[] mRefreshRateValues = new int[]{
            MLX906xx.MLX90640Refresh1HZ,
            MLX906xx.MLX90640Refresh2HZ,
            MLX906xx.MLX90640Refresh4HZ,
            MLX906xx.MLX90640Refresh8HZ,
            MLX906xx.MLX90640Refresh16HZ,
    };

    protected int mlx90640InitializeCheck() {

        int refreshRate = mRefreshRateValues[2];

        if (refreshRate == mMLX90640.getRefreshRate()) {
            return refreshRate;
        }

        int ret = mMLX90640.MLX90640_InitProcedure(refreshRate);
        if (0 != ret) {
//            Toast.makeText(this, "MLX90640初始化失败", Toast.LENGTH_LONG).show();
//            mTvTemperature.setText("MLX90640初始化失败！");
            return ret;
        } else {
//            mTvTemperature.setText("MLX90640初始化成功!");
            return mMLX90640.getRefreshRate();
        }
    }


//    float[] mlx90640ImageP0 = new float[768];
//    float[] mlx90640ImageP1 = new float[768];
//    float[] mlx90640ToP0 = new float[768];
//    float[] mlx90640ToP1 = new float[768];

//    float[] mTemperature = new float[768];
//    float[] mImages = new float[768];

    private TextView tvExpression;

    private void UIInit() {
        tvTemperature = findViewById(R.id.tv_temperature);
        mGridView = findViewById(R.id.grid24x32view);
        cvPreview = findViewById(R.id.cv_preview);
        tvExpression = findViewById(R.id.tv_expression);
        fvFace = findViewById(R.id.fv_face);
        tvResult = findViewById(R.id.tv_result);
        etPwd = findViewById(R.id.et_pwd);
//        btnEncrypt = findViewById(R.id.btn_encrypt);
        fabSwitch = findViewById(R.id.fab_switch);
        ImageView ivBackground = findViewById(R.id.iv_background);

        Glide.with(this).load(R.drawable.access_background).into(ivBackground);
        mGridView.setModuleType(MLXGridView.MLX90640);

        fabSwitch.setOnClickListener(this);
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
//        btnEncrypt.setOnClickListener(this);
//        findViewById(R.id.btn_updateSQL).setOnClickListener(this);

        viewModel.expression.observe(this, expression -> tvExpression.setText(expression));
//        viewModel.temperatureInfoLiveData.observe(this, temperatureInfo -> {
//            float[] temperatures = temperatureInfo.getTemperatures();
//            float[] pixels = temperatureInfo.getPixels();
//            float temperature = temperatureInfo.getFaceTemperature();
//            boolean realFace = temperatureInfo.isRealFace();
//            tvTemperature.setText("当前温度：" + Utils.t1f(temperature) + "度");
//            mGridView.setTemperature(temperatures, pixels);
//            isRealFace = realFace;
//        });

        community = SharedPreferencesUtil.get().loadString("community");
        gate = SharedPreferencesUtil.get().loadString("gate");
        longitude = SharedPreferencesUtil.get().loadString("longitude");
        latitude = SharedPreferencesUtil.get().loadString("latitude");
    }


    @Override
    public void onClick(View view) {
        int resId = view.getId(); // 获得当前按钮的编号
        switch (resId) {
            case R.id.fab_switch:
                if (workThreadFlag) {
                    workThreadFlag = false;
                    findViewById(R.id.cl_camera).setVisibility(View.GONE);
                    findViewById(R.id.cl_keyboard).setVisibility(View.VISIBLE);
                    fabSwitch.setImageResource(R.drawable.ic_saomiao);
                } else {
                    findViewById(R.id.cl_keyboard).setVisibility(View.GONE);
                    findViewById(R.id.cl_camera).setVisibility(View.VISIBLE);
                    fabSwitch.setImageResource(R.drawable.ic_keyboard);
                    workThreadFlag = true;
                    new Thread(new FDThread()).start();
                }
                break;
//            case R.id.btn_encrypt:
//                if (encryption == false) {
//                    btnEncrypt.setText("关闭加密模式");
//                    encryption = true;
////                cvPreview.setVisibility(View.INVISIBLE);
//                } else {
//                    btnEncrypt.setText("开启加密模式");
//                    encryption = false;
////                cvPreview.setVisibility(View.VISIBLE);
//                }
//                break;
            case R.id.button_cancel:
                tvResult.setText("");
                if (password.length() > 0) {
                    password = password.substring(0, password.length() - 1);
                    showText = showText.substring(0, showText.length() - 1);
                }
                etPwd.setText(showText);
                break;
//            case R.id.btn_updateSQL:
//                mFaceDatabase.updateDatabase();
//                break;
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
                etPwd.setText(showText);
                break;
        }

    }

    private class FDThread implements Runnable {
        private String name;
        private String phoneNum;
        private long lastTime;
        private long expressionTime = 0L;
        private long startTime;
        private long temperatureTime = 0L;
        private String QRCode;
        private String lastFacePhoneNum;
        private String lastQRCPhoneNum;
        private Bitmap face;
        private Bitmap faceRect;
        private ExpresionRegcognition expresionRegcognition = new ExpresionRegcognition();
        boolean isRealFace = false;
//        boolean trueFace = false;

        @Override
        public void run() {
            Logger.i(TAG, "start FDThread");
            while (!Thread.currentThread().isInterrupted() && workThreadFlag) {
                startTime = System.currentTimeMillis();
                Bitmap image = cvPreview.getBitmap();
                if (image == null) {
                    continue;
                }
                int width = image.getWidth();
                int height = image.getHeight();
                //镜像水平翻转
                Matrix m = new Matrix();
                m.postScale(-1, 1);
                face = Bitmap.createBitmap(image, 0, 0, width, height, m, true);
                Logger.i(TAG, "取画面并翻转：" + (System.currentTimeMillis() - startTime));

                startTime = System.currentTimeMillis();
                byte[] imageData = getPixelsRGBA(face);    //bitmap转字节数组
                int[] faceInfo = mFace.FaceDetect(imageData, width, height, 4);
                Logger.i(TAG, "人脸检测：" + (System.currentTimeMillis() - startTime));

                startTime = System.currentTimeMillis();
                int num = faceInfo[0];
                if (num > 0) {     //画面中有人脸
                    hasFaceChange=true;
                    Logger.i(TAG, "检测到人脸");
                    //表情识别
//                    long sysTime = new Date().getTime();
                    if (System.currentTimeMillis() - expressionTime > 2000) {
                        expressionTime = System.currentTimeMillis();
                        viewModel.detectFaceEmotion(face);
                    }
                    Logger.i(TAG, "表情识别：" + (System.currentTimeMillis() - startTime));

                    startTime = System.currentTimeMillis();
                    //截取最大的人脸
                    int maxWidth = 0;
                    int j = 0;
                    for (int i = 0; i < num; i++) {
                        int w = faceInfo[14 * i + 3] - faceInfo[14 * i + 1];
                        if (w > maxWidth) {
                            maxWidth = w;
                            j = i;
                        }
                    }
                    int left = faceInfo[14 * j + 1];
                    int top = faceInfo[14 * j + 2];
                    int right = faceInfo[14 * j + 3];
                    int bottom = faceInfo[14 * j + 4];
                    Rect rect = new Rect(left, top, right, bottom);
                    //温度检测

                    if (System.currentTimeMillis() - temperatureTime > 1000) {
                        temperatureTime = System.currentTimeMillis();
//                        viewModel.liveDetect((float) left / width, (float) top / height, (float) right / width, (float) bottom / height);
                        if (mlx90640InitializeCheck() >= 0) {
                            isRealFace = liveDetect((float) left / width, (float) top / height, (float) right / width, (float) bottom / height);
                        }

                        Logger.i(TAG, "温度检测：" + (System.currentTimeMillis() - startTime));
                    }

                    startTime = System.currentTimeMillis();
                    //加密或者画矩形框
                    faceRect = Bitmap.createBitmap(face, left, top, right - left, bottom - top);
                    EncryptFace encryptFace = fvFace.encryptFace(faceRect);
                    Bitmap faceEncrypt = encryptFace.getEncryptFace();
                    Double key = encryptFace.getKey();
                    if (encryption) {
                        fvFace.showEncryptFace(faceEncrypt, rect);
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
                    Object[] objects = mFaceDatabase.featureCmp(feature);   //数据库没内容时会报空指针错误
                    name = (String) objects[0];
                    phoneNum = (String) objects[1];
                    if (!TextUtils.isEmpty(phoneNum)) {     //匹配成功
                        if (isRealFace) {
                            welcome(name);
                            sendMessageDelayed(Constant.CLEAN_TEXT, 2000);
                            long time = System.currentTimeMillis();
                            if (phoneNum.equals(lastFacePhoneNum)) {        //与上一位识别的人相同
                                long interval = System.currentTimeMillis() - time;
                                if (interval > 5000) {
                                    String faceBase64 = Utils.bitmapToBase64(faceEncrypt);
                                    String originalFace = Utils.bitmapToBase64(faceRect);
                                    new Thread(new FDHttpThread(phoneNum, name, faceBase64, key)).start();
                                }
                            } else {            //与上一位识别的人不相同
                                lastFacePhoneNum = phoneNum;
                                String faceBase64 = Utils.bitmapToBase64(faceEncrypt);
                                String originalFace = Utils.bitmapToBase64(faceRect);
                                new Thread(new FDHttpThread(phoneNum, name, faceBase64, key)).start();
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
                    sendMessageDelayed(Constant.CLEAN_EXPRESSION_AND_TEMPERATURE, 1000);
                    if (hasFaceChange&& mlx90640InitializeCheck() >= 0) {
                        for (int i = 0; i < 768; ++i) {
                            temperatures[i] = 0;
                        }
                        Message msg = showHandler.obtainMessage();
                        msg.what = Constant.TEMPERATURE;
                        Bundle data = new Bundle();
                        data.putFloatArray("temperatures", temperatures);
                        msg.setData(data);
                        showHandler.sendMessage(msg);
                    }
                    hasFaceChange=false;

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
//    private boolean liveDetect(float relaLeft, float relaRight, float relaTop, float relaBottom) {
//        int width = 32;
//        int height = 24;
//        int thermalLeft = (int) (width * relaLeft);
//        int thermalRight = (int) (width * relaRight);
//        int thermalTop = (int) (height * relaTop);
//        int thermalBottom = (int) (height * relaBottom);
//        int w = thermalRight - thermalLeft;
//        int h = thermalBottom - thermalTop;
//
//        int faceArea = w * h;
//        int backgroundArea = (thermalBottom + 1) * width - faceArea;
//        int faceCount = 0;
//        int backgroundCount = 0;
//
//
//        int ret = mMLX90640.MLX90640_Measure(mlx90640ImageP0, mlx90640ImageP1, mlx90640ToP0, mlx90640ToP1);
//        Logger.i(TAG, "温度图"+Arrays.toString(mTemperature));
//        float[] max5 = new float[]{0, 0};
//        float min = 0;
//        int index = 0;
//        max = 0L;
//        for (int i = 0; i < 768; ++i) {
//            mTemperature[i] = mlx90640ToP0[i] + mlx90640ToP1[i];
//            mImages[i] = mlx90640ImageP0[i] + mlx90640ImageP1[i];
//
//
//            if (i <= (thermalBottom + 1) * width) { //脖子以上区域
//                if ((i > (thermalTop + 1) * width) && ((i - thermalLeft) % width >= 0) && (i - thermalLeft) % width < (thermalRight - thermalLeft)) {    //脸部区域
////                    mTemperature[i] = 50;
//                    if (max < mTemperature[i]) {
//                        max = mTemperature[i];
//                    }
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
//                    if (mTemperature[i] >= 32 && mTemperature[i] <= 41) {
//                        faceCount++;
//                    }
//
//                } else if (mTemperature[i] >= 30) {
//                    backgroundCount++;
//                }
//            }
//
//            if ((i >= thermalTop * width) && (i < (thermalBottom + 1) * width) &&
//                    (((i > thermalTop * width + thermalLeft) && (i <= thermalTop * width + thermalRight)) ||
//                            ((i > thermalBottom * width + thermalLeft) && (i <= thermalBottom * width + thermalRight)) ||
//                            ((i - thermalLeft) % width == 0) || ((i - thermalRight) % width == 0))) {
//                mTemperature[i] = 20;
//            }
//        }
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
//        float f = (float) faceCount / faceArea;
//        float b = (float) backgroundCount / backgroundArea;
//        Logger.i(TAG, "人脸：" + faceArea + "背景：" + backgroundArea + "人脸正常像素：" + faceCount + "比例：" + f + "背景异常像素：" + backgroundCount + "比例：" + b);
//        return (float) faceCount / faceArea > 0.4;
//    }

//    private void mlx90640Measure() {
//        int ret = AttendanceApplication.MLX90640.MLX90640_Measure(mlx90640ImageP0, mlx90640ImageP1, mlx90640ToP0, mlx90640ToP1);
//
//        float max = Float.MIN_VALUE;
//        for (int i = 0; i < 768; ++i) {
//            ;
//
//            mTemperature[i] = mlx90640ToP0[i] + mlx90640ToP1[i];
//            mImages[i] = mlx90640ImageP0[i] + mlx90640ImageP1[i];
//            if (max < mTemperature[i]) {
//                max = mTemperature[i];
//            }
//        }
//        System.out.println(Arrays.toString(mTemperature));
//        System.out.println(Arrays.toString(mImages));
//        if (ret == 0) {
//            mTvTemperature.setText("当前温度：" + Utils.t1f(max) + "度");
//            mGridView.setTemperature(mTemperature, mImages);
//        } else {
//            mTvTemperature.setText("MLX90640数据读取错误");
//            Toast.makeText(this, "MLX90640数据读取错误", Toast.LENGTH_LONG).show();
//        }
//    }

    /*
    人脸匹配成功，添加记录
     */
    private class FDHttpThread implements Runnable {
        String name, phoneNum, faceBase64;
        Double key;

        FDHttpThread(String phoneNum, String name, String faceBase64, Double key) {
            this.phoneNum = phoneNum;
            this.name = name;
            this.faceBase64 = faceBase64;
            this.key = key;
        }

        @Override
        public void run() {

            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put("service", "door.attendance.enter");
                requestBody.put("phoneNum", phoneNum);
                requestBody.put("name", name);
                requestBody.put("faceBase64", faceBase64);
                requestBody.put("key", key);
                requestBody.put("community", community);
                requestBody.put("building", gate);
                requestBody.put("method", "face");
                requestBody.put("longitude", longitude);
                requestBody.put("latitude", latitude);
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
                requestBody.put("community", community);
                requestBody.put("building", gate);
                requestBody.put("QRCode", QRCode);
                requestBody.put("longitude", longitude);
                requestBody.put("latitude", latitude);
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

    //    float faceTemperature;
//    float max;
    private Handler showHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle data;
            switch (msg.what) {
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
                case Constant.CLEAN_EXPRESSION_AND_TEMPERATURE:
                    tvExpression.setText("");
                    tvTemperature.setText("");
                    break;
                case Constant.TEMPERATURE:
                    data = msg.getData();
                    tvTemperature.setText("当前温度：" + Utils.t1f(data.getFloat("faceTemperature")) + "度");
                    mGridView.setTemperature(data.getFloatArray("temperatures"), data.getFloatArray("pixels"));
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
//    private Bitmap encryptBitmap(Bitmap src) {
//        int w = src.getWidth();
//        int h = src.getHeight();
//        int[] pix = new int[w * h];
//        src.getPixels(pix, 0, w, 0, 0, w, h);
//        int[] newPix = new int[w * h];
//        int blockSize = 20;
//        for (int y = 0; y < h / blockSize; y++) {
//            for (int x = 0; x < w / blockSize; x++) {
//                for (int j = 0; j < blockSize / 2; j++) {
//                    for (int i = 0; i < blockSize; i++) {
//                        if (i < blockSize / 2) {
//                            int index1 = (y * blockSize + j) * w + i + x * blockSize;
//                            int index2 = index1 + (w + 1) * blockSize / 2;
//                            newPix[index1] = pix[index2];
//                            newPix[index2] = pix[index1];
//                        } else {
//                            int index1 = (y * blockSize + j) * w + i + x * blockSize;
//                            int index2 = index1 + (w - 1) * blockSize / 2;
//                            newPix[index1] = pix[index2];
//                            newPix[index2] = pix[index1];
//                        }
//                    }
//                }
//            }
//        }
//        Bitmap res = Bitmap.createBitmap(w, h, src.getConfig());
//        res.setPixels(newPix, 0, w, 0, 0, w, h);
//        return res;
//    }

    /**
     * 测温
     */
    float[] mlx90640ImageP0 = new float[768];
    float[] mlx90640ImageP1 = new float[768];
    float[] mlx90640ToP0 = new float[768];
    float[] mlx90640ToP1 = new float[768];

    float[] temperatures = new float[768];
    float[] pixels = new float[768];

    public boolean liveDetect(float relaLeft, float relaTop, float relaRight, float relaBottom) {

//        Logger.i("人脸坐标",faceRect.toString());
        int width = 32;
        int height = 24;
        int thermalLeft = 0;
        int thermalRight = 0;
        if(relaLeft<0.4){}
            thermalLeft = Math.max((int) (width * relaLeft) - 3, 0);
            thermalRight = Math.max((int) (width * relaRight) - 3, 0);
        if (relaLeft < 0.5) {
            thermalLeft = Math.max((int) (width * relaLeft) - 2, 0);
            thermalRight = Math.max((int) (width * relaRight) - 2, 0);
        } else {
            thermalLeft = Math.max((int) (width * relaLeft) - 1, 0);
            thermalRight = Math.max((int) (width * relaRight) - 1, 0);
        }

        int thermalTop = (int) (height * relaTop);
        int thermalBottom = (int) (height * relaBottom);
        int w = thermalRight - thermalLeft;
        int h = thermalBottom - thermalTop;

        int faceArea = w * h;
        int backgroundArea = (thermalBottom + 1) * width - faceArea;
        int faceCount = 0;
        int backgroundCount = 0;


        long temperatureTime = System.currentTimeMillis();
        int ret = mMLX90640.MLX90640_Measure(mlx90640ImageP0, mlx90640ImageP1, mlx90640ToP0, mlx90640ToP1);
        Logger.i(TAG, "温度检测耗时：" + (System.currentTimeMillis() - temperatureTime));
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
                        if (temperatures[i] >= 30 && temperatures[i] <= 41) {
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
                    temperatures[i] = 50;
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
//            TemperatureInfo temperatureInfo = new TemperatureInfo(temperatures, pixels, faceTemperature, isRealFace);

            Message msg = showHandler.obtainMessage();
            msg.what = Constant.TEMPERATURE;
            Bundle data = new Bundle();
            data.putFloatArray("temperatures", temperatures);
            data.putFloatArray("pixels", pixels);
            data.putFloat("faceTemperature", faceTemperature);
            msg.setData(data);
            showHandler.sendMessage(msg);

            return isRealFace;
        } else {
            Logger.e(TAG, "热成像模块数据读取错误");
            return false;
        }
    }

//    float[] mTemperature = new float[768];
//    float[] mImages = new float[768];
//    private Runnable mAutoRefreshRunnable = null;
//    private void onBtnAuto() {
//        if (null == mAutoRefreshRunnable) {
//            if (mlx90640InitializeCheck() < 0) {
//                return;
//            }
//            mAutoRefreshRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    mlx90640Measure();
//
//                    mHandler.removeCallbacks(this);
//                    mHandler.postDelayed(this, 500);
//                }
//            };
//            mHandler.postDelayed(mAutoRefreshRunnable, 1000);
//        } else {
//            mHandler.removeCallbacks(mAutoRefreshRunnable);
//            mAutoRefreshRunnable = null;
//        }
//    }
//    protected void mlx90640Measure() {
//        int ret = mMLX90640.MLX90640_Measure(mlx90640ImageP0, mlx90640ImageP1, mlx90640ToP0, mlx90640ToP1);
//
//        float max = Float.MIN_VALUE;
//        for (int i = 0; i < 768; ++i) { ;
//
//            mTemperature[i] = mlx90640ToP0[i] + mlx90640ToP1[i];
//            mImages[i] = mlx90640ImageP0[i] + mlx90640ImageP1[i];
//            if (max < mTemperature[i]) {
//                max = mTemperature[i];
//            }
//        }
//        System.out.println(Arrays.toString(mTemperature));
//        System.out.println(Arrays.toString(mImages));
//        if (ret == 0) {
//            tvTemperature.setText("当前温度：" + Utils.t1f(max) +"度");
//            mGridView.setTemperature(mTemperature, mImages);
//        } else {
//            tvTemperature.setText("MLX90640数据读取错误");
//            Toast.makeText(this, "MLX90640数据读取错误", Toast.LENGTH_LONG).show();
//        }
//    }
    /*************************测温结束***************************/
    /**
     * 刷卡
     */
    private void findCard() {

        Runnable mAutoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                rfFelicaGetUidFun();
                handler.removeCallbacks(this);
                handler.postDelayed(this, 600);
            }
        };
        handler.postDelayed(mAutoRefreshRunnable, 1000);
    }

    public void rfFelicaGetUidFun() {
//        tvTip.setText("");
        tvResult.setText("");
        byte[] pData = new byte[16];
        byte[] pLen = new byte[1];
        nStatus = myzrfidAPI.rfGetNameCardUid(nfd, 0, (byte) (0), pData, pLen);
        if (nStatus == 1) {
//            tvTip.setText("寻卡成功！");
            tvResult.setText(ByteArrayToString(pData, (int) (pLen[0])));
        } else {
//            tvTip.setText("寻卡失败！");
        }
    }

    public String ByteArrayToString(byte[] bt_ary, int len) {
        StringBuilder sb = new StringBuilder();
        int i;
        if (bt_ary != null)
            if (len < bt_ary.length) {
                for (i = 0; i < len; i++) {
                    sb.append(String.format("%02X ", bt_ary[i]));
                }
            } else {
                for (byte b : bt_ary) {
                    sb.append(String.format("%02X ", b));
                }
            }
        return sb.toString();
    }

//    public void initUI() {
//        tvTip = findViewById(R.id.tv_tip);
//        tvResult  = findViewById(R.id.tv_card);
//    }

    public void rfInitComFun() {
        nfd = myzrfidAPI.rfInitCom(0, "/dev/ttyS4", 19200);
        if (nfd > 0) {
            nIsOpenCom = 1;
//            tvTip.setText("串口打开成功！");
        } else {
            nIsOpenCom = 0;
//            tvTip.setText("串口打开失败！");
        }
    }

    public void rfClosePortFun() {
        if (nIsOpenCom == 0) {
//            tvTip.setText("串口未打开");
            return;
        }
        nStatus = myzrfidAPI.rfClosePort(nfd, 0);
        if (nStatus == 1) {
//            tvTip.setText("串口关闭成功！");
        } else {
//            tvTip.setText("串口关闭失败！");
        }
    }
    /*****************刷卡模块函数结束**********************/
}