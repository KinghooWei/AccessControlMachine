package com.lztek.tools.irmeter;

/**
 * MLX906xx系列模块操作类.
 */
public class MLX906xx {
    static {
        System.loadLibrary(android.os.Build.VERSION.SDK_INT >= 24? "mlx906xx-71" : "mlx906xx");
        //String libName = android.os.Build.VERSION.SDK_INT >= 24? "mlx906xx-71" : "mlx906xx";
        //try {
        //    System.loadLibrary(libName);
        //} catch (Throwable e) {
        //    android.util.Log.e("#IR#", "[SO]Load lib" + libName + ".so failed: " + e.getMessage(), e);
        //}
    }

    private native int _MLX90621_InitProcedure(int refreshRate);
    private native int _MLX90621_Measure(int refreshRate, float emissivity, float tr,
                                         float[] mlx90621Image, float[] mlx90621To);

    private native int _MLX90640_InitProcedure(int refreshRate);
    private native int _MLX90640_Measure(float emissivity, float tr,
                                         float[] mlx90640ImageP0, float[] mlx90640ImageP1,
                                         float[] mlx90640ToP0, float[] mlx90640ToP1);

    private native int _MLX90614_Measure(float[] temp);

    /*
     * Writes the desired refresh rate value in the appropriate register in order to
     * change the current refresh rate of a MLX90621 device.
     * The return value is 0 if the write was successful, -1 if NACK occurred.
     *
     * @refreshRate: 0x00~0x05=512Hz 0x06~0x0e=256Hz~1Hz(default) 0x0f=0.5Hz
     */
    public static final int  MLX90621Refresh1HZ = 0x000E; /** MLX90621模块刷新频率1赫兹 */
    public static final int  MLX90621Refresh2HZ = 0x000D; /** MLX90621模块刷新频率2赫兹 */
    public static final int  MLX90621Refresh4HZ = 0x000C; /** MLX90621模块刷新频率4赫兹 */
    public static final int  MLX90621Refresh8HZ = 0x000B; /** MLX90621模块刷新频率8赫兹 */
    public static final int  MLX90621Refresh16HZ = 0x000A; /** MLX90621模块刷新频率16赫兹 */
    public static final int  MLX90621Refresh32HZ = 0x0009; /** MLX90621模块刷新频率32赫兹 */
    public static final int  MLX90621Refresh64HZ = 0x0008; /** MLX90621模块刷新频率64赫兹 */
    public static final int  MLX90621Refresh128HZ = 0x0007; /** MLX90621模块刷新频率128赫兹 */
    public static final int  MLX90621Refresh256HZ = 0x0006; /** MLX90621模块刷新频率256赫兹 */

    /*
     * Writes the desired refresh rate value (0x00 to 0x07) in the appropriate register in order to
     * change the current refresh rate of a MLX90640 device with a given slave address. Note that after power -on
     * reset, the refresh rate will revert back to the refresh rate stored in the EEPROM. The return value is 0 if the
     * write was successful, -1 if NACK occurred during the communication and -2 if the written value is not the
     * same as the intended one.
     *
     * @refreshRate: 0x00=0.5Hz 0x01=1Hz 0x02=2Hz(default) 0x03/0x04/0x05/0x06/0x07=4Hz/8Hz/16Hz/32Hz/64Hz
     */
    public static final int  MLX90640Refresh0_5HZ = 0x0000;     /** MLX90640模块刷新频率0.5赫兹 */
    public static final int  MLX90640Refresh1HZ = 0x0001;       /** MLX90640模块刷新频率1赫兹 */
    public static final int  MLX90640Refresh2HZ = 0x0002;       /** MLX90640模块刷新频率2赫兹 */
    public static final int  MLX90640Refresh4HZ = 0x0003;       /** MLX90640模块刷新频率4赫兹 */
    public static final int  MLX90640Refresh8HZ = 0x0004;       /** MLX90640模块刷新频率8赫兹 */
    public static final int  MLX90640Refresh16HZ = 0x0005;      /** MLX90640模块刷新频率16赫兹 */
    public static final int  MLX90640Refresh32HZ = 0x0006;      /** MLX90640模块刷新频率32赫兹 */
    public static final int  MLX90640Refresh64HZ = 0x0007;      /** MLX90640模块刷新频率64赫兹 */

    /**
     * MLX90621模块数据最小长度
     *
     */
    public static final int  MLX90621_BUF_SIZE = 64;

    /**
     * MLX90640模块数据最小长度
     *
     */
    public static final int  MLX90640_BUF_SIZE = 768;

    public static boolean validMLX90621RefreshRate(int refreshRate) {
        return MLX90621Refresh256HZ <= refreshRate &&  refreshRate <= MLX90621Refresh1HZ;
    }

    public static boolean validMLX90640RefreshRate(int refreshRate) {
        return MLX90640Refresh0_5HZ <= refreshRate &&  refreshRate <= MLX90640Refresh64HZ;
    }

    private int  mRefreshRate = -1;
    public int  getRefreshRate() {
        return mRefreshRate;
    }

    /**
     * MLX90621模块初始化，使用默认刷新频率
     *
     * @return 成功返回>=0，出错返回<0
     */
    public int MLX90621_InitProcedure() {
        return MLX90621_InitProcedure(MLX90621Refresh1HZ);
    }

    /**
     * MLX90621模块初始化
     *
     * @param refreshRate
     *             模块刷新频率，可选参数见MLX90621Refresh开头的常量
     *
     * @return 成功返回>=0，出错返回<0
     */
    public int MLX90621_InitProcedure(int refreshRate) {
        if (!validMLX90621RefreshRate(refreshRate)) {
            throw new IllegalStateException("invalid refresh rate for MLX90621!");
        }
        int result = _MLX90621_InitProcedure(refreshRate);
        if (result >= 0) {
            mRefreshRate = refreshRate;
        }
        return result;
    }

    /**
     * MLX90621模块温度、图像数据读取
     *
     * @param refreshRate
     *             模块刷新频率，可选参数见MLX90640Refresh开头的常量
     * @param mlx90621Image
     *             图像数据返回数组，长度必须大于等于MLX90621_BUF_SIZE（64）
     * @param mlx90621To
     *             温度数据返回数组，长度必须大于等于MLX90621_BUF_SIZE（64）
     *
     * @return 成功返回>=0，出错返回<0
     */
    public int MLX90621_Measure(float[] mlx90621Image, float[] mlx90621To) {
        if (null == mlx90621Image || mlx90621Image.length < MLX90621_BUF_SIZE) {
            throw new IllegalStateException("Invalid MLX90621 image0 buffer!");
        }
        if (null == mlx90621To || mlx90621To.length < MLX90621_BUF_SIZE) {
            throw new IllegalStateException("Invalid MLX90621 image1 buffer!");
        }
        if (!validMLX90621RefreshRate(mRefreshRate)) {
            throw new IllegalStateException("MLX90621 is not initialized!");
        }
        return _MLX90621_Measure(mRefreshRate, 0.95f, 0f, mlx90621Image, mlx90621To);
    }

    /**
     * MLX90640模块初始化，使用默认刷新频率
     *
     * @return 成功返回>=0，出错返回<0
     */
    public int MLX90640_InitProcedure() {
        return MLX90640_InitProcedure(MLX90640Refresh2HZ);
    }

    /**
     * MLX90640模块初始化
     *
     * @param refreshRate
     *             模块刷新频率，可选参数见MLX90640Refresh开头的常量
     *
     * @return 成功返回>=0，出错返回<0
     */
    public int MLX90640_InitProcedure(int refreshRate) {
        if (!validMLX90640RefreshRate(refreshRate)) {
            throw new IllegalStateException("invalid refresh rate[" + refreshRate + "] for mlx90640!");
        }
        int result = _MLX90640_InitProcedure(refreshRate);
        if (result >= 0) {
            mRefreshRate = refreshRate;
        }
        return result;
    }

    /**
     * MLX90640模块温度、图像数据读取
     *
     * @param mlx90640ImageP0
     *             奇数帧图像数据返回数组，长度必须大于等于MLX90640_BUF_SIZE（768）
     * @param mlx90640ImageP1
     *             偶数帧图像数据返回数组，长度必须大于等于MLX90640_BUF_SIZE（768）
     * @param mlx90640ToP0
     *             奇数帧温度数据返回数组，长度必须大于等于MLX90640_BUF_SIZE（768）
     * @param mlx90640ToP1
     *             偶数帧温度数据返回数组，长度必须大于等于MLX90640_BUF_SIZE（768）
     *
     * @return 成功返回>=0，出错返回<0
     */
    public int MLX90640_Measure(float[] mlx90640ImageP0, float[] mlx90640ImageP1,
                                float[] mlx90640ToP0, float[] mlx90640ToP1) {
        if (null == mlx90640ImageP0 || mlx90640ImageP0.length < MLX90640_BUF_SIZE) {
            throw new IllegalStateException("Invalid MLX90640 image0 buffer!");
        }
        if (null == mlx90640ImageP1 || mlx90640ImageP1.length < MLX90640_BUF_SIZE) {
            throw new IllegalStateException("Invalid MLX90640 image1 buffer!");
        }
        if (null == mlx90640ToP0 || mlx90640ToP0.length < MLX90640_BUF_SIZE) {
            throw new IllegalStateException("Invalid MLX90640 to0 buffer!");
        }
        if (null == mlx90640ToP1 || mlx90640ToP1.length < MLX90640_BUF_SIZE) {
            throw new IllegalStateException("Invalid MLX90640 to1 buffer!");
        }
        if (!validMLX90640RefreshRate(mRefreshRate)) {
            throw new IllegalStateException("MLX90640 is not initialized!");
        }
        return _MLX90640_Measure(0.95f, 0f, mlx90640ImageP0, mlx90640ImageP1,
                mlx90640ToP0, mlx90640ToP1);
    }

    /**
     * MLX90640模块温度、图像数据读取
     *
     * @param mlx90640ImageGrid
     *             图像数据返回数组，长度必须大于等于MLX90640_BUF_SIZE（768）
     * @param mlx90640ToGrid
     *             温度数据返回数组，长度必须大于等于MLX90640_BUF_SIZE（768）
     *
     * @return 成功返回>=0，出错返回<0
     */
    public int MLX90640_Measure(float[] mlx90640ImageGrid, float[] mlx90640ToGrid) {

        float[] mlx90640ImageP1 = new float[768];
        float[] mlx90640ToP1 = new float[768];

        int result = MLX90640_Measure(mlx90640ImageGrid, mlx90640ImageP1, mlx90640ToGrid, mlx90640ToP1);
        if (result < 0) {
            return result;
        }

        for (int i = 0; i < 768; ++i) {
            mlx90640ToGrid[i] = mlx90640ToGrid[i] + mlx90640ToP1[i];
            mlx90640ImageGrid[i] = mlx90640ImageGrid[i] + mlx90640ImageP1[i];
        }

        return result;
    }

    /**
     * MLX90640模块图像数据读取
     *
     * @param mlx90640ImageP0
     *             奇数帧温度数据返回数组，长度必须大于等于MLX90640_BUF_SIZE（768）
     * @param mlx90640ImageP1
     *             偶数帧温度数据返回数组，长度必须大于等于MLX90640_BUF_SIZE（768）
     *
     * @return 成功返回>=0，出错返回<0
     */
    public int MLX90640_MeasureImage(float[] mlx90640ImageP0, float[] mlx90640ImageP1) {

        if (null == mlx90640ImageP0 || mlx90640ImageP0.length < MLX90640_BUF_SIZE) {
            throw new IllegalStateException("Invalid MLX90640 image0 buffer!");
        }
        if (null == mlx90640ImageP1 || mlx90640ImageP1.length < MLX90640_BUF_SIZE) {
            throw new IllegalStateException("Invalid MLX90640 image1 buffer!");
        }
        if (!validMLX90640RefreshRate(mRefreshRate)) {
            throw new IllegalStateException("MLX90640 is not initialized!");
        }
        return _MLX90640_Measure(0.95f, 0f, mlx90640ImageP0, mlx90640ImageP1, null, null);
    }

    /**
     * MLX90640模块温度数据读取
     *
     * @param mlx90640ToP0
     *             奇数帧温度数据返回数组，长度必须大于等于MLX90640_BUF_SIZE（768）
     * @param mlx90640ToP1
     *             偶数帧温度数据返回数组，长度必须大于等于MLX90640_BUF_SIZE（768）
     *
     * @return 成功返回>=0，出错返回<0
     */
    public int MLX90640_MeasureTemperature(float[] mlx90640ToP0, float[] mlx90640ToP1) {
        if (null == mlx90640ToP0 || mlx90640ToP0.length < MLX90640_BUF_SIZE) {
            throw new IllegalStateException("Invalid MLX90640 to0 buffer!");
        }
        if (null == mlx90640ToP1 || mlx90640ToP1.length < MLX90640_BUF_SIZE) {
            throw new IllegalStateException("Invalid MLX90640 to1 buffer!");
        }
        if (!validMLX90640RefreshRate(mRefreshRate)) {
            throw new IllegalStateException("MLX90640 is not initialized!");
        }
        return _MLX90640_Measure(0.95f, 0f, null, null, mlx90640ToP0, mlx90640ToP1);
    }

    /**
     * MLX90614模块温度、图像数据读取
     *
     * @param temp
     *             温度数据返回数组，长度必须大于等于1，该模块只能近回一个温度值。
     *
     * @return 成功返回>=0，出错返回<0
     */
    public int MLX90614_Measure(float[] temp) {
        return _MLX90614_Measure(temp);
    }
}
