//package com.xwlab.attendance;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.Intent;
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
//import android.os.Environment;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Message;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.util.Size;
//import android.view.Gravity;
//import android.view.Surface;
//import android.view.TextureView;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.VideoView;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.w3c.dom.Text;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.util.Arrays;
//import java.util.Date;
//
//public class AddPersonActivity extends AppCompatActivity {
//    private final String TAG = "AddPersonActivity";
//    public static final String Add_Person_ACTION = "com.xwlab.attendance.Add_Person_ACTION";
//    private Face mFace = new Face();
//    private boolean faceThreadFlag;
//    private Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 0:
//                    Toast.makeText(AddPersonActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
//                    break;
//                default:
//                    Toast.makeText(AddPersonActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
//            }
//        }
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_person_add);
//        Ui_inital();
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
//    }
//
//    private void Ui_inital() {
//        imageView = findViewById(R.id.image);
//        textureView = findViewById(R.id.texture);
//        msgView = findViewById(R.id.message_view);
////        videoView = findViewById(R.id.video);
//        assert textureView != null;
//        // 设置监听
//        textureView.setSurfaceTextureListener(textureListener);
//
//        final Button btnTtakePic = findViewById(R.id.btn_take_picture);
//        btnTtakePic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (btnTtakePic.getText().equals("拍照")) {
//                    imageView.setImageBitmap(face);
//                    bitmapFace = face.copy(Bitmap.Config.ARGB_8888, true);
//                    btnTtakePic.setText("重拍");
//                } else {
//                    imageView.setImageBitmap(null);
//                    bitmapFace = null;
//                    btnTtakePic.setText("拍照");
//                }
//
//            }
//        });
//
//        Button btnAddOrUpdate = findViewById(R.id.btn_add_or_update);
//        btnAddOrUpdate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                EditText editName = findViewById(R.id.edit_name);
//                String personName = editName.getText().toString();
//                EditText editStudentNum = findViewById(R.id.edit_student_num);
//                String studentNum = editStudentNum.getText().toString();
//                if (bitmapFace == null) {
//                    Toast.makeText(AddPersonActivity.this, "头像为空！上传失败", Toast.LENGTH_SHORT).show();
//                    return;
//                } else if (personName.equals("")) {
//                    Toast.makeText(AddPersonActivity.this, "姓名为空！上传失败", Toast.LENGTH_SHORT).show();
//                    return;
//                } else if (studentNum.equals("")) {
//                    Toast.makeText(AddPersonActivity.this, "学号为空！上传失败", Toast.LENGTH_SHORT).show();
//                    return;
//                } else {
//                    Toast.makeText(AddPersonActivity.this, "正在上传...", Toast.LENGTH_SHORT).show();
//                    addPersonToServer(bitmapFace, studentNum, personName, 0);
//                    imageView.setImageBitmap(null);
//                    bitmapFace = null;
//                    btnTtakePic.setText("拍照");
//                }
//            }
//        });
//        Button btnMain = findViewById(R.id.btn_main);
//        btnMain.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(new SwitchActivityThread()).start();
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
//            AddPersonActivity.this.cameraDevice = null;
//        }
//
//        //        打开摄像头出现错误时激发方法
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            cameraDevice.close();
//            AddPersonActivity.this.cameraDevice = null;
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
//                    Toast.makeText(AddPersonActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
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
//                ActivityCompat.requestPermissions(AddPersonActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
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
//    ////////////////////////// 人脸识别线程 ///////////////////////////////
//    public class FDThread extends Thread {
//        @Override
//        public void run() {
//            try {
//                Logger.i(TAG, "start add person FDThread");
//                while (!Thread.currentThread().isInterrupted()) {
//                    if (!faceThreadFlag) {
//                        Logger.i(TAG, "addPersonActivity 人脸识别线程被终止...");
//                        return;
//                    }
//                    Bitmap image = textureView.getBitmap();
//                    if (image == null) {
//                        continue;
//                    }
//                    int width = image.getWidth();
//                    int height = image.getHeight();
//                    byte[] imageDate = getPixelsRGBA(image);
//                    long timeDetectFace = System.currentTimeMillis();
//                    int[] faceInfo = mFace.FaceDetect(imageDate, width, height, 4);
//                    Logger.i(TAG, "get faceInfo");
//                    if (faceInfo[0] > 0) {
//                        long systime = new Date().getTime();//当前系统时间
//                        int cx = (faceInfo[1] + faceInfo[3]) / 2;
//                        int cy = (faceInfo[2] + faceInfo[4]) / 2;
//                        int w = faceInfo[3] - faceInfo[1];
//                        int h = faceInfo[4] - faceInfo[2];
//                        int nx = cx - w > 0 ? cx - w : 0;
//                        int ny = cy - h > 0 ? cy - h : 0;
//                        int nw = nx + 2 * w < image.getWidth() ? 2 * w : image.getWidth() - nx;
//                        int nh = ny + 2 * h < image.getHeight() ? 2 * h : image.getHeight() - ny;
//                        Logger.i(TAG, "data is " + nx + " " + ny + " " + nw + " " + nh);
//                        face = Bitmap.createBitmap(image, nx, ny, nw, nh);
//                    }
//                }
//            } catch (Exception e) {
//                Logger.e(TAG, "FD error");
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private byte[] getPixelsRGBA(Bitmap image) {
//        // calculate how many bytes our image consists of
//        int bytes = image.getByteCount();
//        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create lastTime new buffer
//        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
//        byte[] temp = buffer.array(); // Get the underlying array containing the
//        return temp;
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
//        java.io.OutputStream myOutput = new FileOutputStream(sdDir.toString() + "/attendance/" + strOutFileName);
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
//    private class SwitchActivityThread extends Thread {
//        @Override
//        public void run() {
//            faceThreadFlag = false;
//            try {
//                Thread.sleep(1000);
//                startActivity(new Intent(AddPersonActivity.this, MainActivity.class));
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }
//
//        }
//    }
//
//    private void addPersonToServer(Bitmap bitmap, final String studentNum, final String personName, final int encryption) {
//        File sdDir = Environment.getExternalStorageDirectory();
//        String tmpName = sdDir.toString() + "/personToAdd.jpg"; //临时图片文件
//        final File file = new File(tmpName);
//        try {
//            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//            bos.flush();
//            bos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        new Thread() {
//            @Override
//            public void run() {
//                Log.i("HttpTest", "start->");
//                JSONObject jsonObject = new JSONObject();
//                String timestamp = HttpUtils.getSecondTimestamp();   //获取当前的时间戳
//                try {
//                    jsonObject.put("service", "door.person.add");
//                    jsonObject.put("studentNum", studentNum);
//                    jsonObject.put("studentNum", studentNum);
//                    jsonObject.put("personName", personName);
//                    jsonObject.put("timestamp", HttpUtils.getSecondTimestamp());
//                    //updataTime_last 表示上次更新本地数据库的时间，本次会更新从这个时间之后新增的数据
//                } catch (JSONException ex) {
//                    ex.printStackTrace();
//                }
//                String result = HttpUtils.sendMultiPartPost(file, jsonObject.toString());
//                Log.i(TAG, "addPerson result is:" + result);
//                try {
//                    JSONObject resultJson = new JSONObject(result);
//                    int resultCode = resultJson.getInt("resultCode");
//                    Message msg = mHandler.obtainMessage();
//                    msg.what = resultCode;
//                    mHandler.sendMessage(msg);
//                } catch (JSONException ex) {
//                    ex.printStackTrace();
//                    Message msg = mHandler.obtainMessage();
//                    msg.what = -1;
//                    mHandler.sendMessage(msg);
//                } catch (NullPointerException ex) {
//                    ex.printStackTrace();
//                    Message msg = mHandler.obtainMessage();
//                    msg.what = -1;
//                    mHandler.sendMessage(msg);
//                }
//            }
//        }.start();
//    }
//}
