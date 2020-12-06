package com.xwlab.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.xwlab.attendance.Utils;

public class MLXGridView extends View {
    public MLXGridView(Context context) {
        super(context);//引用父类的构造函数
        initView();
    }

    public MLXGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MLXGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public static final int MLX90640 = 1;
    public static final int MLX90621 = 2;
    public static final int MLX90614 = 3;

    private Paint mLinePaint = null;//分隔线画笔
    private Paint mRectPaint = null;//渐变方块画笔
    private Paint mTextPaint = null;//文字
    float[] mTemp = new float[768];
    float[] mImages = new float[768];

    private int mModuleType = MLX90640;

    private void initView() {
        this.setBackgroundColor(Color.TRANSPARENT);//背景设置透明

        if (null == mLinePaint) {
            mLinePaint = new Paint();
            mLinePaint.setColor(Color.BLACK);
            mLinePaint.setAntiAlias(true);
            mLinePaint.setStrokeWidth(2.0f);//笔款
        }
        if (null == mRectPaint) {
            mRectPaint = new Paint();
            mRectPaint.setAntiAlias(true);
            mRectPaint.setStyle(Paint.Style.FILL);
        }
        if (null == mTextPaint) {
            mTextPaint = new Paint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setColor(Color.BLACK);
            mTextPaint.setTextSize(18);
        }

        float step = mTemp.length > 1? TEMPERATURE_RANGE/(mTemp.length-1) : TEMPERATURE_RANGE;
        for (int i=0; i<mTemp.length; ++i) {
            mTemp[i] = TEMPERATURE_MIN + i*step;
            mImages[i] = convertColor(mTemp[i]);
        }
    }


    public void setModuleType(int moduleType) {
        if (MLX90640 == moduleType) {
            mTemp = new float[768]; // 24*32
        } else if (MLX90621 == moduleType) {
            mTemp = new float[64]; // 4*16
        } else if (MLX90614 == moduleType) {
            mTemp = new float[1];
        } else {
            throw new IllegalArgumentException("Invalid module type: " + moduleType);
        }

        mModuleType = moduleType;
        mImages = new float[mTemp.length];

        float step = mTemp.length > 1? TEMPERATURE_RANGE/(mTemp.length-1) : TEMPERATURE_RANGE;
        for (int i=0; i<mTemp.length; ++i) {
            mTemp[i] = TEMPERATURE_MIN + i*step;
            mImages[i] = convertColor(mTemp[i]);
        }
    }

    private static float TEMPERATURE_MIN = 28.0f;
    private static float TEMPERATURE_MAX = 35.0f;
    private static float TEMPERATURE_RANGE = TEMPERATURE_MAX - TEMPERATURE_MIN;
    private static int convertColor(float temperature) {
        float gray = (temperature <= TEMPERATURE_MIN ? 0 : (temperature >= TEMPERATURE_MAX ? 255 :
                (int)(((temperature- TEMPERATURE_MIN)/ TEMPERATURE_RANGE)*255)));
      int rgbone=0;   int rgbtwo=0;   int rgbthree=0;
        if ((gray>=0) && (gray<=63)) {
            rgbone=0;
            rgbtwo=0;
            rgbthree=Math.round((gray/64)*255);


        } else if ((gray>=64) && (gray<=127)){
            rgbone=0;
            rgbtwo=Math.round((gray-64)/64*255);
            rgbthree=Math.round((127-gray)/64*255);

        } else if ((gray>=128) && (gray<=191)){
            rgbone=Math.round((gray-128)/64*255);
            rgbtwo=255;
            rgbthree=0;

        } else if ((gray>=192) && (gray<=255)) {
            rgbone = 255;
            rgbtwo = Math.round((255 - gray)/64 * 255);
            rgbthree = 0;
        }

        return Color.rgb(rgbone,rgbtwo,rgbthree);
       // return Color.rgb(((gray>=0)&&(gray<=127) ? 0 : (gray >= 192 ? 255: Math.round((gray-128)/64*255))),
           //        ((gray>=0)&&(gray<=63) ? 0 : ((gray>=64)&&(gray<=127)?Math.round((gray-64)/64*255):(gray>=192?Math.round((255 - gray)/64 * 255):255))),
            //      ((gray>=0)&&(gray<=63) ? Math.round((gray/64)*255): (gray>=128?0:Math.round((127-gray)/64*255))));


       /*return Color.rgb((gray >= 0 ? gray : 0 - gray),
              (gray >= 127 ? gray - 127 : 127 - gray),
              (gray >= 255 ? gray - 255 : 255 - gray));
       */

       // return Color.rgb(   (Math.abs(0-gray) ), (Math.abs(127 - gray)), ( Math.abs(255 - gray)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.drawColor(Color.WHITE, android.graphics.PorterDuff.Mode.CLEAR);

        if (MLX90640 == mModuleType) {
            onDrawGrid(canvas, 32, 24);
        } else if (MLX90621 == mModuleType) {
            onDrawGrid(canvas, 16, 4);
        } else if (MLX90614 == mModuleType) {
            onDrawMLX90614(canvas);
        }
    }

    protected void onDrawGrid(Canvas canvas, int colNum, int rowNum) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        int cellWidth = canvasWidth/colNum;
        int cellHeight = canvasHeight/rowNum;

        int gridWidth =  cellWidth*colNum;
        int gridHeight = cellHeight*rowNum;

        int oddWidth = canvasWidth - gridWidth;
        int oddHeight = canvasHeight - gridHeight;

        int startX = 0;
        int startY = 0;

        Rect rect = new Rect(startX, startY, startX+cellWidth, startY+cellHeight);

        mTextPaint.setTextSize(cellHeight*40/100);
        Paint.FontMetrics metric = mTextPaint.getFontMetrics();
        float y = cellHeight/2-(metric.ascent+metric.descent)/2;

        float max = Float.MIN_VALUE;
        Rect maxRect = new Rect(rect);
        int index = 0;

        for (int row=0; row<rowNum; ++row) {
            rect.bottom = rect.top + cellHeight + (oddHeight-row > 0? 1 : 0);

            rect.left = startX;
            for (int col=0; col<colNum; ++col) {
                rect.right = rect.left + cellWidth + (oddWidth-col > 0? 1 : 0);

                if (mTemp[index] > max) {
                    max = mTemp[index];
                    maxRect.set(rect);
                }

                mRectPaint.setColor(convertColor(mTemp[index]));
                //mRectPaint.setColor((int)mImages[index]);
                canvas.drawRect(rect, mRectPaint);

                rect.left = rect.right;
                index++;
            };
            rect.top = rect.bottom;
        }

        canvas.drawText(Utils.t1f(max), maxRect.left, maxRect.top+y, mTextPaint);
    }


    protected void onDrawMLX90614(Canvas canvas) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        int centerX = canvasWidth/2;
        int centerY = canvasHeight/2;
        int radius = canvasWidth < canvasHeight? canvasWidth/4 : canvasHeight/4;

        mTextPaint.setTextSize(20);
        Paint.FontMetrics metric = mTextPaint.getFontMetrics();

        canvas.drawLines(new float[]{0, 0, canvasWidth, 0,
                canvasWidth, 0, canvasWidth, canvasHeight,
                canvasWidth, canvasHeight, 0, canvasHeight,
                0, canvasHeight, 0, 0}, mLinePaint);

        mRectPaint.setColor(convertColor(mTemp[0]));
        //mRectPaint.setColor((int)mImages[0]);
        canvas.drawCircle(centerX, centerY, radius, mRectPaint);
        canvas.drawText(Utils.t1f(mTemp[0]),
                centerX + radius + 5, centerY - (metric.ascent+metric.descent)/2, mTextPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        int widthScale = MLX90640 == mModuleType? 32 : 16;
        int heightScale = MLX90640 == mModuleType? 24 : 4;

        if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
            width = widthSpecSize;
            height = heightSpecSize;
            if ( widthScale * height  < width * heightScale) {
                width = height * widthScale / heightScale;
            } else if ( widthScale * height  > width * heightScale) {
                height = width * heightScale / widthScale;
            }
        } else if (widthSpecMode == MeasureSpec.EXACTLY) {
            width = widthSpecSize;
            height = width * heightScale / widthScale;
            if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                height = heightSpecSize;
            }
        } else if (heightSpecMode == MeasureSpec.EXACTLY) {
            height = heightSpecSize;
            width = height * widthScale / heightScale;
            if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                width = widthSpecSize;
            }
        } else {
            width = widthSpecSize;
            height = heightSpecSize;

            if ( widthScale * height  < width * heightScale) {
                width = height * widthScale / heightScale;
            } else if ( widthScale * height  > width * heightScale) {
                height = width * heightScale / widthScale;
            }
        }

        setMeasuredDimension(width, height);
    }

    public void setTemperature(float[] temperatures, float[] images) {
        if (null != temperatures && null != mTemp) {
            System.arraycopy(temperatures, 0, mTemp, 0,
                    mTemp.length < temperatures.length ? mTemp.length : temperatures.length);
        }
        if (null != images && null != mImages) {
            System.arraycopy(images, 0, mImages, 0,
                    mImages.length < images.length? mImages.length : images.length);
        }
        invalidate();
    }
}