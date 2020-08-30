//package com.xwlab.attendance;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.PorterDuff;
//import android.graphics.PorterDuffXfermode;
//import android.graphics.SurfaceTexture;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CameraMetadata;
//import android.hardware.camera2.CaptureRequest;
//import android.hardware.camera2.params.StreamConfigurationMap;
//import android.media.ImageReader;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Message;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Size;
//import android.view.Gravity;
//import android.view.Surface;
//import android.view.TextureView;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.util.Arrays;
//import java.util.Date;
//
//public class MainActivity extends AppCompatActivity {
//    private final String TAG = "MainActivity";
//    private Toast mToast;
//    private Face mFace = new Face();
//    private FaceDatabase myFaceDatabase;
//    private boolean faceThreadFlag = true;
//    // Used to load the 'native-lib' library on application startup.
////    static {
////        System.loadLibrary("native-lib");
////    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Ui_initial();
//        // Example of a call to a native method
////        TextView tv = findViewById(R.id.sample_text);
////        tv.setText(stringFromJNI());
//        try {
//            copyBigDataToSD("det1.bin");
//            copyBigDataToSD("det2.bin");
//            copyBigDataToSD("det3.bin");
//            copyBigDataToSD("det1.param");
//            copyBigDataToSD("det2.param");
//            copyBigDataToSD("det3.param");
//            copyBigDataToSD("recognition.bin");
//            copyBigDataToSD("recognition.param");
////            copyBigDataToSD("test.csv");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        File sdDir = Environment.getExternalStorageDirectory();//get directory
//        String sdPath = sdDir.toString() + "/attendance/";
//        mFace.FaceModelInit(sdPath);
//        myFaceDatabase = new FaceDatabase(getApplicationContext());
//        myFaceDatabase.updateDatabase();
//    }
//
//    /**
//     * A native method that is implemented by the 'native-lib' native library,
//     * which is packaged with this application.
//     */
//    public native String stringFromJNI();
//
//    private void Ui_initial() {
////        if (mToast == null) {
////            mToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);
////            LinearLayout layout = (LinearLayout) mToast.getView();
////            TextView tv = (TextView) layout.getChildAt(0);
////            tv.setTextSize(35);
////        }
//        imageView = findViewById(R.id.image);
//        textureView = findViewById(R.id.texture);
//        msgView = findViewById(R.id.message_view);
////        videoView = findViewById(R.id.video);
//        assert textureView != null;
//        // 设置监听
//        textureView.setSurfaceTextureListener(textureListener);
//
//        final Button btnEncrption = findViewById(R.id.btn_encryption);
//        btnEncrption.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (encryption == 0) {
//                    btnEncrption.setText("关闭加密模式");
//                    encryption = 1;
//                } else {
//                    btnEncrption.setText("开启加密模式");
//                    encryption = 0;
//                }
//            }
//        });
//
//    }
//
//    ////////////////////// 开启摄像头并预览 /////////////////////////////////////////////
//    //    摄像头ID，一般0是后视，1是前视
//    private String cameraId;
//    //定义代表摄像头的成员变量，代表系统摄像头，该类的功能类似早期的Camera类。
//    protected CameraDevice cameraDevice;
//    // 定义CameraCaptureSession成员变量，是一个拍摄会话的类，用来从摄像头拍摄图像或是重新拍摄
//    protected CameraCaptureSession cameraCaptureSessions;
//    //    当程序调用setRepeatingRequest()方法进行预览时，或调用capture()进行拍照时，都需要传入CaptureRequest参数时
//    //    captureRequest代表一次捕获请求，用于描述捕获图片的各种参数设置。比如对焦模式，曝光模式...等，程序对照片所做的各种控制，都通过CaptureRequest参数来进行设置
//    //    CaptureRequest.Builder 负责生成captureRequest对象
//    protected CaptureRequest.Builder captureRequestBuilder;
//    private static final int REQUEST_CAMERA_PERMISSION = 300;
//    // 预览尺寸
//    private Size imageDimension;
//    //     ImageReader allow direct application access to image data rendered into lastTime Surface
//    private ImageReader imageReader;
//    private TextureView textureView;
//    private TextView msgView;
//    private ImageView imageView;
//    private Bitmap bitmapFace, face;
//    private int encryption = 1;
//    private String personName;
//    private String personId;
//    private Handler mBackgroundHandler;
//    private HandlerThread mBackgroundThread;
//    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//            openCamera();
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//            // Transform you image captured size according to the surface width and height
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//            return true;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        }
//    };
//
//    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @Override
////        摄像头打开激发该方法
//        public void onOpened(CameraDevice camera) {
//
//            Logger.e(TAG, "onOpened");
//            cameraDevice = camera;
////            开始预览
//            createCameraPreview();
//        }
//
//        //        摄像头断开连接时的方法
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//            cameraDevice.close();
//            MainActivity.this.cameraDevice = null;
//        }
//
//        //        打开摄像头出现错误时激发方法
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            cameraDevice.close();
//            MainActivity.this.cameraDevice = null;
//        }
//    };
//
//    protected void createCameraPreview() {
//        try {
//            SurfaceTexture texture = textureView.getSurfaceTexture();
//            assert texture != null;
////            设置默认的预览大小
//            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
//            Surface surface = new Surface(texture);
////            请求预览
//            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//
//            captureRequestBuilder.addTarget(surface);
////            创建cameraCaptureSession,第一个参数是图片集合，封装了所有图片surface,第二个参数用来监听这处创建过程
//            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    //The camera is already closed
//                    if (null == cameraDevice) {
//                        return;
//                    }
//                    // When the session is ready, we start displaying the preview.
//                    cameraCaptureSessions = cameraCaptureSession;
//                    updatePreview();
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void openCamera() {
////        实例化摄像头
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        Logger.e(TAG, "is camera open");
//        try {
////            指定要打开的摄像头
//            cameraId = manager.getCameraIdList()[1];
////            获取打开摄像头的属性
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
////The available stream configurations that this camera device supports; also includes the minimum frame durations and the stall durations for each format/size combination.
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            assert map != null;
//            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
//            // Add permission for camera and let user grant the permission
////            权限检查
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
//                return;
//            }
////            打开摄像头，第一个参数代表要打开的摄像头，第二个参数用于监测打开摄像头的当前状态，第三个参数表示执行callback的Handler,
////            如果程序希望在当前线程中执行callback，像下面的设置为null即可。
//            manager.openCamera(cameraId, stateCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        Logger.e(TAG, "openCamera 1");
//    }
//
//    protected void updatePreview() {
//        if (null == cameraDevice) {
//            Logger.e(TAG, "updatePreview error, return");
//        }
////        设置模式为自动
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        try {
//            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected void startBackgroundThread() {
//        mBackgroundThread = new HandlerThread("Camera Background");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }
//
//    protected void stopBackgroundThread() {
//        mBackgroundThread.quitSafely();
//        try {
//            mBackgroundThread.join();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void closeCamera() {
//        if (null != cameraDevice) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//        if (null != imageReader) {
//            imageReader.close();
//            imageReader = null;
//        }
//
//    }
//
//    ////////////////////// 开启摄像头并预览 end /////////////////////////////////////////////
//    private Boolean haveface = false;//用来判断是否有脸
//    private String lastName = "";
//    private final int unknownTimeInit = 3;
//    private int unknownTime = unknownTimeInit;
//    private long lastTime;
//    private final int UPDATE_FACE = 0;
//    private final int CLOSE_FACE = 1;
//    private final int SHOW_TOAST = 2;
//    private Handler MyHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case UPDATE_FACE:
//                    imageView.setImageBitmap(face);
////                    Bitmap.createBitmap(face, 1,1,101, 100);
////                    nameTextView.setText(personName);
//                    break;
//                case CLOSE_FACE:
//                    imageView.setImageBitmap(null);
//                    msgView.setText("");
////                    nameTextView.setText(null);
//                    break;
//                case SHOW_TOAST:
//                    Bundle data = msg.getData();
//                    String message = data.getString("message");
//                    msgView.setText(message);
//                    Logger.i(TAG, "验证成功 in show tip " + personName);
////                    //mToast.cancel();
////                    mToast.setGravity(Gravity.CENTER, 0, 0);
////                    mToast.setText(message);
////                    mToast.show();
//            }
//        }
//    };
//    ////////////////////////// 人脸识别线程 ///////////////////////////////
//
//    private String lastUnknownFeature = "";
//    private Long lastUnknownTime = 0L;
//
//    public class FDThread extends Thread {
//        @Override
//        public void run() {
//            try {
//                Logger.i(TAG, "start FDThread");
//                while (!Thread.currentThread().isInterrupted()) {
//                    if (!faceThreadFlag) {
//                        Logger.i(TAG, "Main activity 的人脸识别线程被终止...");
//                        return;
//                    }
////                    if(!myFaceDatabase.isInitialed()){
////                        Logger.i(TAG, "数据库未初始化完成...");
////                        continue;
////                    }
//                    Bitmap image = textureView.getBitmap();
//                    if (image == null) {
//                        continue;
//                    }
//                    int width = image.getWidth();
//                    int height = image.getHeight();
//                    byte[] imageDate = getPixelsRGBA(image); //bitmap转字节数组
//                    long timeDetectFace = System.currentTimeMillis();
//                    int[] faceInfo = mFace.FaceDetect(imageDate, width, height, 4);//返回坐标
//                    if (faceInfo[0] > 0) {
//                        long systime = new Date().getTime();//当前系统时间
//                        Bitmap faceRect = Bitmap.createBitmap(image, faceInfo[1], faceInfo[2],
//                                faceInfo[3] - faceInfo[1], faceInfo[4] - faceInfo[2]);
//                        //裁剪，faceInfo[1]：第一个像素的x坐标，faceInfo[2]：第二个像素的y坐标
//                        int widthcan = faceRect.getWidth();
//                        Paint paint = new Paint();
//                        paint.setAntiAlias(true);
//                        Bitmap circleBitmap = Bitmap.createBitmap(widthcan, widthcan, Bitmap.Config.ARGB_8888);//取原图
//                        Canvas canvas = new Canvas(circleBitmap);
//                        canvas.drawCircle(widthcan / 2f, widthcan / 2f, widthcan / 2f, paint);//在原图上画园
//                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//在原图和画的圆上取交集得到circleBitmap
//                        canvas.drawBitmap(faceRect, 0, 0, paint);
////                        face = circleBitmap;
//                        if (encryption == 1) face = encryptBitmap(circleBitmap);
//                        else face = circleBitmap;
//                        Message msg1 = MyHandler.obtainMessage();
//                        msg1.what = UPDATE_FACE;
//                        MyHandler.sendMessage(msg1);
//                        timeDetectFace = System.currentTimeMillis() - timeDetectFace;
//                        Logger.i(TAG, "face detect cost " + timeDetectFace);
//                        byte[] faceDate = getPixelsRGBA(faceRect);
////                        byte[] faceDate = bitmapToBytes(faceRect);
//                        String feature = mFace.FaceFeatureRestore(faceDate, faceRect.getWidth(), faceRect.getHeight());//获取待验证的人脸特征描述
//                        Logger.i(TAG, "get feature: " + feature);
//                        Object[] objects = myFaceDatabase.featureCmp(feature);      //人脸匹配，返回相匹配的人名
//                        personName = (String) objects[0];
//                        personId = (String) objects[1];
//                        Logger.i(TAG, "recognize person: " + personName);
//                        if (personName.equals("unknown")) {
//                            Logger.i(TAG, "unknown");
//                            double sim = myFaceDatabase.calculSimilar(feature, lastUnknownFeature);
//                            Logger.i(TAG, "sim : " + sim);
//                            if (sim > 0.6) {
//                                Long time = systime - lastUnknownTime;//
//                                if (time > 60000) {
//                                    lastUnknownTime = systime;
//                                    showToast("未知人员");
//                                    HttpService.reportUnknown(face, encryption);
//                                }
//                            } else {
//                                lastUnknownFeature = feature;
//                                lastUnknownTime = systime;
//                                Logger.i(TAG, "未知人员-new");
//                                showToast("未知人员");
//                                HttpService.reportUnknown(face, encryption);
//                            }
//                            haveface = false;
//                        } else if (personName.equals("")) {
//                            haveface = false;
//                        } else if (personName.equals(lastName)) {
//                            Long time = systime - lastTime;//对于同一个人毫秒差，一秒内出现多次只算一次开门
//                            haveface = time > 1000;
//                            Logger.i(TAG, "Time is " + time);
//                            if (haveface) lastTime = systime;
//                        } else {
//                            haveface = true;
//                            lastName = personName;
//                            lastTime = systime;
//                        }
//                    } else {
//                        haveface = false;
//                        Message msg1 = MyHandler.obtainMessage();
//                        msg1.what = CLOSE_FACE;
//                        MyHandler.sendMessage(msg1);
//                    }
//                    if (haveface) {
//                        Logger.i(TAG, "验证成功 in FD " + personName);
//                        showToast("验证成功: " + personName);
//                        HttpService.reportAttendance(personName, personId, encryption);
//                    }
//                }
//            } catch (Exception e) {
//                Logger.e(TAG, "FD error");
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public class QRCodeThread extends Thread {
//        @Override
//        public void run() {
//            Logger.i(TAG, "start QRCodeThread");
//            while (!Thread.currentThread().isInterrupted()) {
//                if (!faceThreadFlag) {
//                    Logger.i(TAG, "Main activity 的二维码识别线程被终止...");
//                    return;
//                }
////                    if(!myFaceDatabase.isInitialed()){
////                        Logger.i(TAG, "数据库未初始化完成...");
////                        continue;
////                    }
//                Bitmap image = textureView.getBitmap();
//                if (image == null) {
//                    continue;
//                }
//
//
//            }
//        }
//    }
//
//    /**
//     * bitmap转base64
//     *
//     * @param bitmap
//     * @return
//     */
//    private static byte[] bitmapToBytes(Bitmap bitmap) {
//        String result = null;
//        ByteArrayOutputStream baos = null;
//        byte[] bitmapBytes = new byte[0];
//        try {
//            if (bitmap != null) {
//                baos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//
//                baos.flush();
//                baos.close();
//
//                bitmapBytes = baos.toByteArray();
////                result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (baos != null) {
//                    baos.flush();
//                    baos.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return bitmapBytes;
//    }
//
//
//    //get pixels
//    private byte[] getPixelsRGBA(Bitmap image) {
//        // calculate how many bytes our image consists of
//        int bytes = image.getByteCount();
//        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create lastTime new buffer
//        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
//        byte[] temp = buffer.array(); // Get the underlying array containing the
//        return temp;
//    }
//
//    private Bitmap encryptBitmap(Bitmap src) {
//        // calculate how many bytes our image consists of
//        int w = src.getWidth();
//        int h = src.getHeight();
//
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
////        int[] newNewPix = new int[w * h];
////        for(int y=0;y<h/blockSize;y++){
////            for(int x=0;x<w/blockSize;x++){
////                for(int j=0;j<blockSize/2;j++){
////                    for(int i=0;i<blockSize;i++){
////                        if(i<blockSize/2){
////                            int index1 = (y*blockSize+j)*w+i+x*blockSize;
////                            int index2 = index1 + (w+1)*blockSize/2;
////                            newNewPix[index1] = newPix[index2];
////                            newNewPix[index2] = newPix[index1];
////                        }else{
////                            int index1 = (y*blockSize+j)*w+i+x*blockSize;
////                            int index2 = index1 + (w-1)*blockSize/2;
////                            newNewPix[index1] = newPix[index2];
////                            newNewPix[index2] = newPix[index1];
////                        }
////                    }
////                }
////            }
////        }
//        Bitmap res = Bitmap.createBitmap(w, h, src.getConfig());
//        res.setPixels(newPix, 0, w, 0, 0, w, h);
//        return res;
//    }
//
//    public void showToast(String message) {
//        Message msg1 = MyHandler.obtainMessage();
//        msg1.what = SHOW_TOAST;
//        Bundle data = new Bundle();
//        data.putString("message", message);
//        msg1.setData(data);
//        MyHandler.sendMessage(msg1);
//    }
//
//    ////////////////////////// 自定义toast ///////////////////////////////
//    private void showTip(final String str) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
////                Toast newToast = Toast.makeText(getApplicationContext(), str,Toast.LENGTH_LONG);
////                LinearLayout layout = (LinearLayout) newToast.getView();
////                TextView tv = (TextView) layout.getChildAt(0);
////                tv.setTextSize(35);
////                newToast.setGravity(Gravity.CENTER, 0, 0);
////                newToast.show();
//                if (mToast == null) {
//                    mToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);
//                    LinearLayout layout = (LinearLayout) mToast.getView();
//                    TextView tv = (TextView) layout.getChildAt(0);
//                    tv.setTextSize(35);
//                }
//                Logger.i(TAG, "验证成功 in show tip " + personName);
//                //mToast.cancel();
//                mToast.setGravity(Gravity.CENTER, 0, 0);
//                mToast.setText(str);
//                mToast.show();
//            }
//        });
//    }
//
//    private void copyBigDataToSD(String strOutFileName) throws IOException {
//        Logger.i(TAG, "start copy file " + strOutFileName);
//        File sdDir = Environment.getExternalStorageDirectory();//get directory
//        File file = new File(sdDir.toString() + "/attendance/");
//        if (!file.exists()) {
//            file.mkdir();
//        }
//        String tmpFile = sdDir.toString() + "/attendance/" + strOutFileName;
//        File f = new File(tmpFile);
//        if (f.exists()) {
//            Logger.i(TAG, "file exists " + strOutFileName);
//            return;
//        }
//        InputStream myInput;
//        java.io.OutputStream myOutput = new FileOutputStream(sdDir.toString()
//                + "/attendance/" + strOutFileName);
//        myInput = this.getAssets().open(strOutFileName);
//        byte[] buffer = new byte[1024];
//        int length = myInput.read(buffer);
//        while (length > 0) {
//            myOutput.write(buffer, 0, length);
//            length = myInput.read(buffer);
//        }
//        myOutput.flush();
//        myInput.close();
//        myOutput.close();
//        Logger.i(TAG, "end copy file " + strOutFileName);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        faceThreadFlag = false;
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        faceThreadFlag = true;
//        new Thread(new FDThread()).start();
//    }
//
//
////    private class SwitchActivityThread extends Thread {
////        @Override
////        public void run() {
////            faceThreadFlag = false;
////            try {
////                Thread.sleep(1000);
////                startActivity(new Intent(MainActivity.this, AddPersonActivity.class));
////            } catch (InterruptedException ex) {
////                ex.printStackTrace();
////            }
////
////        }
////    }
//}
