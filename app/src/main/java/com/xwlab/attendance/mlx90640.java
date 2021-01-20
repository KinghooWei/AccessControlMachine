//package com.xwlab.attendance;
//
//import android.os.Handler;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.lztek.tools.irmeter.MLX906xx;
//import com.xwlab.util.Constant;
//import com.xwlab.widget.MLXGridView;
//
//import java.util.Arrays;
//
//public class mlx90640 {
//    private static final String TAG = "热成像";
//    protected Handler mHandler;
//
//    protected MLX906xx mMLX90640;
//    protected TextView mTvTemperature;
//    protected MLXGridView mGridView;
//
//    private int[] mRefreshRateValues = new int[]{
//            MLX906xx.MLX90640Refresh1HZ,
//            MLX906xx.MLX90640Refresh2HZ,
//            MLX906xx.MLX90640Refresh4HZ,
//            MLX906xx.MLX90640Refresh8HZ,
//            MLX906xx.MLX90640Refresh16HZ,
//    };
//
//    protected int mlx90640InitializeCheck() {
//
//        int refreshRate = mRefreshRateValues[4];
//
//        if (refreshRate == mMLX90640.getRefreshRate()) {
//            return refreshRate;
//        }
//
//        int ret = mMLX90640.MLX90640_InitProcedure(refreshRate);
//        if (0 != ret) {
//            Utils.showToast("MLX90640初始化失败");
//            return ret;
//        } else {
//            Utils.showToast("MLX90640初始化成功");
//            return mMLX90640.getRefreshRate();
//        }
//    }
//
//
//    float[] mlx90640ImageP0 = new float[768];
//    float[] mlx90640ImageP1 = new float[768];
//    float[] mlx90640ToP0 = new float[768];
//    float[] mlx90640ToP1 = new float[768];
//
//    float[] mTemperature = new float[768];
//    float[] mImages = new float[768];
//
//    protected void mlx90640Measure() {
//        int ret = mMLX90640.MLX90640_Measure(mlx90640ImageP0, mlx90640ImageP1, mlx90640ToP0, mlx90640ToP1);
//
//        float max = Float.MIN_VALUE;
//        for (int i = 0; i < 768; ++i) {
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
//        }
//    }
//
//    /**
//     * 活体检测
//     */
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
//        int faceArea = w*h;
//        int backgroundArea = (thermalBottom + 1) * width - faceArea;
//        int faceCount = 0;
//        int backgroundCount = 0;
//
//        if (mlx90640InitializeCheck() < 0) {
//            return false;
//        }
//        int ret = mMLX90640.MLX90640_Measure(mlx90640ImageP0, mlx90640ImageP1, mlx90640ToP0, mlx90640ToP1);
//        Logger.i(TAG, "温度图"+Arrays.toString(mTemperature));
//        max = Float.MIN_VALUE;
//        for (int i = 0; i < 768; ++i) {
//            mTemperature[i] = mlx90640ToP0[i] + mlx90640ToP1[i];
//            mImages[i] = mlx90640ImageP0[i] + mlx90640ImageP1[i];
//            if (max < mTemperature[i]) {
//                max = mTemperature[i];
//            }
//
//
//            if (i <= (thermalBottom + 1) * width) {
//                if (mTemperature[i] >= 26 && mTemperature[i] <= 41 && (i > (thermalTop + 1) * width) && ((i - thermalLeft) % width >= 0) && (i - thermalRight) % width < (thermalRight - thermalLeft)) {
//                    faceCount++;
//                } else if (mTemperature[i] >= 30){
//                    backgroundCount++;
//                }
//            }
//
//            if ((i >= thermalTop * width) && (i < (thermalBottom +1) * width) &&
//                    (((i > thermalTop * width + thermalLeft) && (i <= thermalTop * width + thermalRight)) ||
//                            ((i > thermalBottom * width + thermalLeft) && (i <= thermalBottom * width + thermalRight)) ||
//                            ((i - thermalLeft) % width == 0) || ((i - thermalRight) % width == 0))) {
//                mTemperature[i] = 50;
//            }
//        }
//        System.out.println(Arrays.toString(mTemperature));
//        System.out.println(Arrays.toString(mImages));
//        if (ret == 0) {
//            sendMessage(Constant.THERMAL);
////            mTvTemperature.setText("当前温度：" + Utils.t1f(max) + "度");
////            mGridView.setTemperature(mTemperature, mImages);
//        } else {
//            mTvTemperature.setText("MLX90640数据读取错误");
//        }
//        float f = (float) faceCount / faceArea;
//        float b = (float) backgroundCount / backgroundArea;
//        Logger.i(TAG, "人脸："+faceArea+"背景："+backgroundArea+"人脸正常像素："+faceCount+"比例："+f+"背景异常像素："+backgroundCount+"比例："+b);
//        return (float) faceCount / faceArea > 0.6 && (float) backgroundCount / backgroundArea < 0.4;
//    }
//}
