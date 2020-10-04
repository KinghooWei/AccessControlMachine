package com.xwlab.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;

import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Random;


public class FaceView extends View {
    private static final int EMPTY = 0;         //动画状态-没有
    private static final int RECT = 1;        //动画状态-开启
    private static final int ENCRYPT = 2;      //动画状态-结束

    private Context mContext;           // 上下文
    private int mWidth, mHeight;        // 宽高
    private Handler mHandler;           // handler

    private Paint mPaint;
    private Bitmap mBitmap;
    private Rect pos;

    private int animCurrentPage = -1;       // 当前页码
    private int animMaxPage = 13;           // 总页数
    private int animDuration = 500;         // 动画时长
    private int canvasState = EMPTY;      // 动画状态

    private boolean isCheck = false;        // 是否只选中状态

    public FaceView(Context context) {
        this(context, null);
    }

    public FaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    //初始化
    private void init(Context context) {
        mContext = context;
        setBackgroundColor(Color.TRANSPARENT);
//        mPaint = new Paint();
//        mPaint.setColor(0xffFF5317);
//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setAntiAlias(true);

//        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.checkmark);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                invalidate();
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(0, 0);
        switch (canvasState) {
            case EMPTY:
                Path path= new Path();
                path.reset();
                break;
            case RECT:
                Paint paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                canvas.drawRect(pos, paint);
                break;
            case ENCRYPT:
                if (mBitmap!=null) {
                    int sideHeight = mBitmap.getHeight();
                    int sideWidth = mBitmap.getWidth();
                    // 得到图像选区 和 实际绘制位置
                    Rect src = new Rect(0, 0, sideWidth, sideHeight);
                    // 绘制
                    canvas.drawBitmap(mBitmap, src, pos, null);
                }
                break;
        }
        // 绘制背景圆形
//        canvas.drawCircle(0, 0, 240, mPaint);

        // 得出图像边长

    }

    public void drawRect(Rect rect) {
        canvasState = RECT;
        pos = rect;
//        invalidate();
        mHandler.sendEmptyMessage(0);
    }

    public Bitmap encryptFace(Bitmap face) {

        double key = keyValue(face);          					// 密钥值
        // 加密
        return processBitmap(face, key);
    }

    public void showEncryptFace(Bitmap face, Rect rect) {
        canvasState = ENCRYPT;
        mBitmap = face;
        pos = rect;
        mHandler.sendEmptyMessage(0);
    }

    public void clearCanvas() {
        canvasState = EMPTY;
        mHandler.sendEmptyMessage(0);
    }

    /**
     * 设置背景圆形颜色
     *
//     * @param color
     */
//    public void setBackgroundColor(int color) {
//        mPaint.setColor(color);
//    }

    // 根据位图上随机某个pixel的值求密钥值key
    protected static double keyValue(Bitmap bitmap) {
        int h = bitmap.getHeight();							// 位图高度
        int w = bitmap.getWidth();							// 位图宽度
        int y = new Random().nextInt(h);        // 获得一个[0, h]区间内的随机整数
        int x = new Random().nextInt(w);        // 获得一个[0, w]区间内的随机整数
        int p = Math.abs(bitmap.getPixel(x, y));
        return  (double) p / Math.pow(10, String.valueOf(p).length());
    }

    // 加解密程序
    protected static Bitmap processBitmap(Bitmap bitmap, double key) {
        Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        int h = bitmap.getHeight();							// 位图高度
        int w = bitmap.getWidth();							// 位图宽度
        int mArrayColorLength = h * w;
        int[] s = sequenceGenerator(key, mArrayColorLength);	// 设置Logistic混沌系统初始值和迭代次数
        int[] mArrayColor = new int[mArrayColorLength];
        int[] bArray = new int[mArrayColorLength];
        bitmap.getPixels(bArray, 0, w, 0, 0, w, h);
        // 遍历位图
        for (int i = 0; i < mArrayColorLength; i++) {
            mArrayColor[i] = bArray[i] ^ s[i];                           // 位图像素值与混沌序列值作异或
        }
        newBitmap.setPixels(mArrayColor, 0, w, 0, 0, w, h);    // 为新位图赋值
        return newBitmap;
    }

    // 产生logistic混沌序列
    protected static int[] sequenceGenerator(double x0, int timeStep) {
        final double u = 3.9;                        // 控制参数u
        double[] x = new double[timeStep + 1000];

        x[0] = x0;
        // 迭代产生混沌序列，长度为 “timeStep+1000”
        for (int i = 0; i < timeStep + 999; i++) {
            x[i + 1] = u * x[i] * (1 - x[i]);       // 一维Logistic混沌系统
        }

        double[] new_x = Arrays.copyOfRange(x, 1000, timeStep + 1000);    // 去除前1000个混沌值，去除暂态效应
        int[] seq = new int[timeStep];
        // 处理混沌序列值
        for (int i = 0; i < timeStep; i++) {
            new_x[i] = new_x[i] * Math.pow(10, 4) - Math.floor(new_x[i] * Math.pow(10, 4));
            seq[i] = (int) Math.floor(Math.pow(10, 9) * new_x[i]);
        }
        return seq;
    }

    public void clear(Canvas canvas) {
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        invalidate();
    }

}
