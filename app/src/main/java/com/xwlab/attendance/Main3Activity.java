package com.xwlab.attendance;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.hardware.camera2.CameraCharacteristics;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.xwlab.util.CodeHints;
import com.xwlab.util.Constant;
import com.xwlab.widget.Camera2View;

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
    private boolean faceThreadFlag = true;
    private static File sdDir = Environment.getExternalStorageDirectory();
    private static final String sdPath = sdDir.toString() + "/attendance/";
    private Camera2View cvPreview;
    private ImageView ivFace;
    private TextView tvResult, etPwd;
    private String password = "", showText = "";
    private int encryption = 0;     //1表示开启加密模式
    private Bitmap face;
    private Uri ringtoneUri = Uri.parse("android.resource://" + R.raw.beep);
    private Ringtone ringtone;

    private String lastUnknownFeature;
    private Long lastUnknownTime = 0L;

    private boolean haveFace = false;
    private String lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        mFace.FaceModelInit(sdPath);
        mFaceDatabase = new FaceDatabase(getApplicationContext());
        mFaceDatabase.updateDatabase();
        UIInit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        faceThreadFlag = true;
        new Thread(new FDThread()).start();
        new Thread(new QRCodeScanThread()).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        faceThreadFlag = false;
    }

    private void UIInit() {
        cvPreview = (Camera2View) findViewById(R.id.cv_preview);
        ivFace = findViewById(R.id.iv_face);
        tvResult = findViewById(R.id.tv_result);
        etPwd = findViewById(R.id.et_pwd);
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
        cvPreview.open(CameraCharacteristics.LENS_FACING_BACK);//平板
//        cvPreview.open(CameraCharacteristics.LENS_FACING_FRONT);//3288
//        cvPreview.open(CameraCharacteristics.LENS_FACING_EXTERNAL);//3288
    }


    @Override
    public void onClick(View view) {
        int resId = view.getId(); // 获得当前按钮的编号
        String inputText;

        if (resId == R.id.btn_encrypt) {
            Button btnEncrypt = findViewById(R.id.btn_encrypt);
            if (btnEncrypt.getText() == "开启加密模式") {
                btnEncrypt.setText("关闭加密模式");
                encryption = 1;
                cvPreview.setVisibility(View.INVISIBLE);
            } else {
                btnEncrypt.setText("开启加密模式");
                encryption = 0;
                cvPreview.setVisibility(View.VISIBLE);
            }
        } else if (resId == R.id.button_cancel) {
            tvResult.setText("");
            if (password.length() > 0) {
                password = password.substring(0, password.length() - 1);
                showText = showText.substring(0, showText.length() - 1);
            }
        } else {
            if (password.length() < 6) {
                inputText = ((TextView) view).getText().toString();
                password = password + inputText;
                showText = showText + '·';
            }
            if (password.length() == 6) {
                if (mFaceDatabase.passwordCmp(password)) {
                    tvResult.setText("验证成功");
                } else {
                    tvResult.setText("密码输入错误");
                }
                showText = "";
                password = "";
            } else {
                tvResult.setText("");
            }
        }
        etPwd.setText(showText);
    }


    private class FDThread implements Runnable {
        private String name, phoneNum;
        private Long lastTime;

        @Override
        public void run() {
            Logger.i(TAG, "start FDThread");
            while (!Thread.currentThread().isInterrupted()) {
                if (!faceThreadFlag) {
                    Logger.i(TAG, "stop FDThread");
                    return;
                }
                Bitmap image = cvPreview.getBitmap();
                if (image == null) {
                    continue;
                }
                int width = image.getWidth();
                int height = image.getHeight();
                //bitmap转字节数组
                byte[] imageData = getPixelsRGBA(image);
                long timeDetectFace = System.currentTimeMillis();
                //实时人脸检测
//                Logger.i(TAG, "实时检测人脸");
                int[] faceInfo = mFace.FaceDetect(imageData, width, height, 4);
                if (faceInfo[0] == 1) {     //画面中有1张人脸
                    Logger.i(TAG, "检测到人脸");
                    long sysTime = new Date().getTime();
                    //截取人脸
                    Bitmap faceRect = Bitmap.createBitmap(image, faceInfo[1], faceInfo[2], faceInfo[3] - faceInfo[1], faceInfo[4] - faceInfo[2]);
                    int widthCan = faceRect.getWidth();
                    Paint paint = new Paint();
                    Bitmap circleBitmap = Bitmap.createBitmap(widthCan, widthCan, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(circleBitmap);
                    canvas.drawCircle(widthCan / 2f, widthCan / 2f, width / 2f, paint);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                    canvas.drawBitmap(faceRect, 0, 0, paint);
                    if (encryption == 1) {

                        face = encryptBitmap(circleBitmap);
                    } else {
                        face = circleBitmap;
                    }
                    sendMessage(Constant.UPDATE_FACE);
                    timeDetectFace = System.currentTimeMillis() - timeDetectFace;
//                    Logger.i(TAG, "face detect cost " + timeDetectFace);
                    //人脸转字节数组
                    byte[] faceDate = getPixelsRGBA(faceRect);
                    //特征提取
                    String feature = mFace.FaceFeatureRestore(faceDate, faceRect.getWidth(), faceRect.getHeight());
                    Object[] objects = mFaceDatabase.featureCmp(feature);
                    name = (String) objects[0];
                    phoneNum = (String) objects[1];
                    Logger.i(TAG, "人脸识别成功：" + name);

                    if (name.equals("unknown")) {
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

                    } else if (name.equals("")) {
                        haveFace = false;
                    } else if (name.equals(lastName)) {
                        Long time = sysTime - lastTime;
                        haveFace = time > 1000;
                        if (haveFace) {
                            new Thread(new FDHttpThread(phoneNum, name)).start();
                            lastTime = sysTime;
                        }
                    } else {
                        haveFace = true;
                        lastName = name;
                        lastTime = sysTime;
                        new Thread(new FDHttpThread(phoneNum, name)).start();
                    }
                } else {
                    haveFace = false;
                    sendMessage(Constant.CLOSE_FACE);

                }
                if (haveFace) {
                    welcome(name);
//                    HttpService.reportAttendance(name, phoneNum, encryption);
//                    try {
//                        sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    sendMessage(Constant.CLEAN_TEXT);
                }
            }
        }
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

    private class QRCodeScanThread implements Runnable {
        private String QRCode, phoneNum, lastPhoneNum;
        private Long lastTime;
        private boolean hasQR;

        @Override
        public void run() {
            Logger.e(TAG, "start QRThread");
            while (!Thread.currentThread().isInterrupted()) {
                if (!faceThreadFlag) {
                    return;
                }
                Bitmap image = cvPreview.getBitmap();
                if (image == null) {
                    continue;
                }
                Result result = decodeQR(image);
                if (result != null) {
                    String[] data = result.toString().split("-");
                    if (data.length == 3 && data[0].equals("blueCity")) {   //是目标二维码
//                        Logger.e(TAG,result.toString());
                        Long sysTime = new Date().getTime();
//                        Logger.i(TAG, sysTime.toString());
                        if (data[1].equals(lastPhoneNum)) {     //与上一个二维码相同
                            Long time = sysTime - lastTime;
//                            Logger.i(TAG, time.toString());
                            if (time > 2000) {     //间隔大于2秒
                                phoneNum = data[1];
                                QRCode = data[2];
                                if (ringtone != null) {
                                    // 停止播放铃声
                                    ringtone.stop();
                                }
                                ringtone = RingtoneManager.getRingtone(Main3Activity.this, ringtoneUri);
                                ringtone.play();
                                new Thread(new QRCodeHttpThread(phoneNum, QRCode)).start();
                            }
                        } else {        //与上一个二维码不同
                            phoneNum = data[1];
                            QRCode = data[2];
                            if (ringtone != null) {
                                // 停止播放铃声
                                ringtone.stop();
                            }
                            ringtone = RingtoneManager.getRingtone(Main3Activity.this, ringtoneUri);
                            ringtone.play();
                            new Thread(new QRCodeHttpThread(phoneNum, QRCode)).start();
                        }
                        lastTime = sysTime;
                        lastPhoneNum = phoneNum;
                    }
                } else {
//                    sendMessage(Constant.CLEAN_TEXT);
                }
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
//                    sleep(2000);
//                    sendMessage(Constant.CLEAN_TEXT);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

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
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void sendMessage(int what) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    private void welcome(String name) {
        Message msg = mHandler.obtainMessage();
        msg.what = Constant.WELCOME;
        Bundle data = new Bundle();
        data.putString("name", name);
        msg.setData(data);
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle data;
            switch (msg.what) {
                case Constant.UPDATE_FACE:
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
            }
        }
    };

    private byte[] getPixelsRGBA(Bitmap image) {
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        image.copyPixelsToBuffer(buffer);
        return buffer.array();
    }

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
