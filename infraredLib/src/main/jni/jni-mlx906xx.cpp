#include <stdio.h> 
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <sys/wait.h>

#include <sys/system_properties.h>
#include <assert.h>

#include <jni.h>
#include <android/log.h>

#include "mlx90614_api.h"
#include "mlx90621_api.h"
#include "mlx90640_api.h"

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#define LOGD(fmt, args...) do{ __android_log_print(ANDROID_LOG_DEBUG, "#IR#", fmt, ##args); printf(fmt, ##args); }while(0)
#define LOGE(fmt, args...) do{ __android_log_print(ANDROID_LOG_ERROR, "#IR#", fmt, ##args); printf(fmt, ##args); }while(0)

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


#define DUMP_IMAGE  0
#define DUMP_TEMP   0

/* Varialbe defines for MLX90621
 */
static unsigned char eeData_21[256];
static unsigned short frameData_21[66];
static paramsMLX90621 mlx90621;

/* Varialbe defines for MLX90640
 */
static uint16_t eeData_40[832];
static uint16_t frameData_40[834];
static paramsMLX90640 mlx90640;

static float mlx90640ToP0[768];
static float mlx90640ToP1[768];
static float mlx90640ImageP0[768];
static float mlx90640ImageP1[768];

#define ARRAY_LENGTH(_array)                    (sizeof(_array)/sizeof(_array[0]))

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

static const char gClassName[] = "com/lztek/tools/irmeter/MLX906xx";
JNIEXPORT jint MLX90621_InitProcedure(JNIEnv *env, jobject thiz, jint refreshRate);
JNIEXPORT jint MLX90621_Measure(JNIEnv *env, jobject thiz, jint refreshRate, jfloat emissivity, jfloat tr,
                                 jfloatArray jmlx90621Image, jfloatArray jmlx90621To);

JNIEXPORT jint MLX90640_InitProcedure(JNIEnv *env, jobject thiz, jint refreshRate);
JNIEXPORT jint MLX90640_Measure(JNIEnv *env, jobject thiz, jfloat emissivity, jfloat tr,
                                jfloatArray jmlx90640ImageP0, jfloatArray jmlx90640ImageP1,
                                jfloatArray jmlx90640ToP0, jfloatArray jmlx90640ToP1);
JNIEXPORT jint MLX90614_Measure(JNIEnv *env, jobject thiz, jfloatArray temp);

static JNINativeMethod gExportMethods[] =
{ 
        {
                "_MLX90621_InitProcedure",
                "(I)I",
                (void*)MLX90621_InitProcedure
        },
        {
                "_MLX90621_Measure",
                "(IFF[F[F)I",
                (void*)MLX90621_Measure
        },
        {
                "_MLX90640_InitProcedure",
                "(I)I",
                (void*)MLX90640_InitProcedure
        },
        {
                "_MLX90640_Measure",
                "(FF[F[F[F[F)I",
                (void *) MLX90640_Measure
        },
        {
                "_MLX90614_Measure",
                "([F)I",
                (void *) MLX90614_Measure
        },
};

int dev_i2c_4_node_chmod();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env = NULL;
	jclass clazz;
	int methodsLenght;

	if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK)
	{
    	LOGE("JNI_VERSION ERROR");
		return JNI_ERR;
	}
	assert(env != NULL);

	clazz = env->FindClass(gClassName);
	if (clazz == NULL)
	{
    	LOGE("Cannot find class %s", gClassName);
		return JNI_ERR;
	}
	methodsLenght = sizeof(gExportMethods) / sizeof(gExportMethods[0]);
	if (env->RegisterNatives(clazz, gExportMethods, methodsLenght) < 0)
	{
    	LOGE("####### class %s RegisterNatives failed", gClassName);
		return JNI_ERR;
	}

    dev_i2c_4_node_chmod();

	return JNI_VERSION_1_4;
}

void JNI_OnUnload(JavaVM* vm, void* reserved)
{
}

int dev_i2c_4_node_chmod()
{
	const char* path = "/dev/i2c-4";
	if (access(path, R_OK|W_OK) == 0)
		return 0;

    //jclass versionClass = env->FindClass("android/os/Build$VERSION");
    //jfieldID fieldId = env->GetStaticFieldID(versionClass, "SDK_INT", "I");
    //jint sdkInt = env->GetStaticIntField(versionClass, fieldId);
    //env->DeleteLocalRef(versionClass);

    char sdk_int[128];
    memset(sdk_int, 0, sizeof(sdk_int));
    __system_property_get("ro.build.version.sdk", sdk_int);
    int sdkInt = atoi(sdk_int);

    char command[1024];
	memset(command, 0, sizeof(command));

    const char* shell = NULL;

	if (sdkInt >= 24)
	{
		if (access("/system/xbin/daemonsu", F_OK) == 0)
            shell = "su"; // sprintf(command, "su chmod %d %s", 666, path);
		else
			sprintf(command, "su root chmod %d %s", 666, path);
	}
	else
	{
		if (access("/system/xbin/xid", F_OK) == 0)
			sprintf(command, "xid %d%d chmod %d %s", 1024, 6579, 666, path);
		else
			shell = "su"; // "su chmod %d %s", 666, path);
	}

    if (shell)
    {
        FILE* fp = NULL; 
    	fp = popen(shell, "w");
    	if (!fp)
    	{
        	LOGE("### cannot popen command: %s -- [%d]: %s\n", shell, errno, strerror(errno));
        	return -1;
    	} 
    	else 
    	{  
    	    int result = fprintf(fp, "chmod %d %s\n", 666, path);
    	    if (result < 0)
        	    LOGE("### chmod write failed: [%d]: %s\n", errno, strerror(errno));
    	    pclose(fp);   
    	    LOGE("## chmod result: %d\n", result);
    	}
    } 
    else
    {
    	// system(command);
    	int result = system(command);
    	//LOGE("[%d]Commnad: %s\n", result, command);
    	LOGE("## chmod result: %d\n", result);
    	if (result != -1)
    	{
    		if (WIFEXITED(result))
    		{
    			LOGE("## chmod result code: %d\n", WEXITSTATUS(result));
    		}
    		else
    		{
    			LOGE("!! chmod result code: %d\n", WEXITSTATUS(result));
    		}
    	}
    }
	
	int times =0;
	while (times < 2500)
	{
		usleep(50*1000);
		times += 50;
		if (access(path, R_OK|W_OK) == 0)
			return 0;
	}

	return -1;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


/*
 * Should execute an initialization procedure first or upon POR reload.
 * Upon success it is ONLY required to be called once.
 *
 * @refreshRate: 0x00~0x05=512Hz 0x06~0x0e=256Hz~1Hz(default) 0x0f=0.5Hz
 */
//int MLX90621_InitProcedure(uint8_t refreshRate)
jint MLX90621_InitProcedure(JNIEnv *env, jobject thiz, jint refreshRate)
{
    uint16_t cfgReg;
    int refreshRate0;
    int resolutionRAM;

    memset(&mlx90621, 0, sizeof(mlx90621));

    if (MLX90621_DumpEE(eeData_21) < 0) {
        LOGE("mlx90621 dump eeprom failed\n");
        return -1;
    }

    if (MLX90621_Configure(eeData_21) < 0) {
        LOGE("mlx90621 set configuration failed\n");
        return -1;
    }

    MLX90621_ExtractParameters(eeData_21, &mlx90621);

    /* 0000~0101=512Hz 0110~1110=256Hz~1Hz(default) 1111=0.5Hz
     */
    refreshRate0 = MLX90621_GetRefreshRate();
    LOGD("==> Config refresh rate is %d\n", refreshRate0);
    if (refreshRate != refreshRate0) {
        if (MLX90621_SetRefreshRate(refreshRate) < 0) {
            LOGE("mlx90621 set refresh rate failed\n");
            return -1;
        }
        LOGD("==> Runtime refresh rate is %d\n", refreshRate);
    }

    /* 0=15-bit 1=16-bit 2=17-bit 3=18-bit(default)
     */
    resolutionRAM = MLX90621_GetCurResolution();
    LOGD("==> Runtime resolution is %d\n", resolutionRAM);

    return 0;
}

/*
 * Read image array and object temperature and return 0 if success
 * Raw image array and calculated temperature are 16x4 matrix with 64 pixels.
 *
 * @refreshRate: 0x00~0x05=512Hz 0x06~0x0e=256Hz~1Hz(default) 0x0f=0.5Hz
 *
 * @emissivity: Emissivity defined by the user. The emissivity is a property of the measured object.
 *
 * @tr: Reflected temperature defined by the user. If the object emissivity is less than 1, there might be some
 * temperature reflected from the object. In order for this to be compensated the user should input this
 * reflected temperature. The sensor ambient temperature could be used, but some shift depending on the
 * enclosure might be needed. For a MLX90640 in the open air the shift is -8°C.
 */
//int MLX90621_Measure(uint8_t refreshRate, float emissivity, float tr)
jint MLX90621_Measure(JNIEnv *env, jobject thiz, jint refreshRate, jfloat emissivity, jfloat tr,
                                jfloatArray jmlx90621Image, jfloatArray jmlx90621To)
{
    int i;

    float mlx90621Image[64];
    float mlx90621To[64];
    float averageTo = 0;
    float maximumTo = -100.0f;
    float minimumTo =  100.0f;


    if (jmlx90621Image && env->GetArrayLength(jmlx90621Image) < ARRAY_LENGTH(mlx90621Image)) {
        return -1;
    }
    if (jmlx90621To && env->GetArrayLength(jmlx90621To) < ARRAY_LENGTH(jmlx90621Image)) {
        return -2;
    }

    if (MLX90621_CheckReloadStatus()) {
        usleep(5*1000);
        if (MLX90621_InitProcedure(env, thiz, refreshRate) < 0) {
            return -3;
        }
    }

    if (MLX90621_GetFrameData(frameData_21) < 0) {
        LOGE("mlx90621 get frame data failed\n");
        return -4;
    }

    if (tr == 0) {
        tr = MLX90621_GetTa(frameData_21, &mlx90621);
        tr = tr - TA_SHIFT;
    }

    /* Get image without calculate the temperature and ONLY for hot image
     */
    LOGD("==> IR image data[64]\n");
    MLX90621_GetImage(frameData_21, &mlx90621, mlx90621Image);
    for (i = 0; i < 64; i++) {
#if DUMP_IMAGE
        LOGD(" [%02d] = %f ", i, mlx90621Image[i]);
#endif
    }

    averageTo = 0;
    MLX90621_CalculateTo(frameData_21, &mlx90621, mlx90621To);
    for (i = 0; i < 64; i++) {
        averageTo += mlx90621To[i];
        if (minimumTo > mlx90621To[i])
            minimumTo = mlx90621To[i];
        if (maximumTo < mlx90621To[i])
            maximumTo = mlx90621To[i];
#if DUMP_TEMP
        LOGD("[%02d] = %2.2f ", i, mlx90621To[i]);
#endif
    }
    LOGD("==> To min=%2.2f max=%2.2f average=%2.2f\n", minimumTo, maximumTo, averageTo/64);

    averageTo = 0;
    maximumTo = -100.0f;
    minimumTo =  100.0f;
    MLX90621_CalculateTo_2(frameData_21, &mlx90621, emissivity, tr, mlx90621To);
    for (i = 0; i < 64; i++) {
        averageTo += mlx90621To[i];
        if (minimumTo > mlx90621To[i])
            minimumTo = mlx90621To[i];
        if (maximumTo < mlx90621To[i])
            maximumTo = mlx90621To[i];
#if DUMP_TEMP
        LOGD(" [%02d] = %2.2f ", i, mlx90621To[i]);
#endif
    }
    LOGD("==> To2 min=%2.2f max=%2.2f average=%2.2f\n", minimumTo, maximumTo, averageTo/64);

    if (jmlx90621Image)
        env->SetFloatArrayRegion(jmlx90621Image, 0, ARRAY_LENGTH(mlx90621Image), mlx90621Image);
    if (jmlx90621To)
        env->SetFloatArrayRegion(jmlx90621To, 0, ARRAY_LENGTH(mlx90621To), mlx90621To);

    return 0;
}

///////////////////////////////////////////////////////////////////////////////

/*
 * Should execute an initialization procedure first or upon POR reload
 * Upon success it is ONLY required to be called once.
 *
 * @refreshRate: 0x00=0.5Hz 0x01=1Hz 0x02=2Hz(default) 0x03/0x04/0x05/0x06/0x07=4Hz/8Hz/16Hz/32Hz/64Hz
 */
//int MLX90640_InitProcedure(uint8_t refreshRate)
jint MLX90640_InitProcedure(JNIEnv *env, jobject thiz, jint refreshRate)
{
    uint16_t cfgReg;
    int mode;
    int refreshRate0;
    int resolutionRAM;

    memset(&mlx90640, 0, sizeof(mlx90640));

    if (MLX90640_DumpEE(eeData_40) < 0)
    {
        LOGE("mlx90640 dump eeprom error\n");
        return -1;
    }

    MLX90640_ExtractParameters(eeData_40, &mlx90640);

    /* 0=Interleaved (TV) mode 1=Chess pattern (default)
     */
    mode = MLX90640_GetCurMode();
    LOGD("==> Runtime reading pattern is %d\n", mode);

    /* 0=0.5Hz 1=1Hz 2=2Hz(default) 3/4/5/6/7=4Hz/8Hz/16Hz/32Hz/64Hz
     */
    refreshRate0 = MLX90640_GetRefreshRate();
    LOGD("==> Config refresh rate is %d\n", refreshRate0);
    if (refreshRate != refreshRate0) {
        if (MLX90640_SetRefreshRate(refreshRate) < 0) {
            LOGE("mlx90640 set refresh rate failed\n");
            return -1;
        }
        LOGD("==> Runtime refresh rate is %d\n", refreshRate);
    }

    /* 0=16-bit 1=17-bit 2=18-bit(default) 3=19-bit
     */
    resolutionRAM = MLX90640_GetCurResolution();
    LOGD("==> Runtime resolution is %d\n", resolutionRAM);

    return 0;
}


/*
* Read image array and object temperature and return 0 if success.
* Raw image array and calculated temperature are 32x24 matrix with 768 pixels.
*
* Emissivity defined by the user. The emissivity is a property of the measured object.
*
* Reflected temperature defined by the user. If the object emissivity is less than 1, there might be some
* temperature reflected from the object. In order for this to be compensated the user should input this
* reflected temperature. The sensor ambient temperature could be used, but some shift depending on the
* enclosure might be needed. For a MLX90640 in the open air the shift is -8°C.
*/
//int MLX90640_Measure(float emissivity, float tr)
jint MLX90640_Measure(JNIEnv *env, jobject thiz, jfloat emissivity, jfloat tr,
                                jfloatArray jmlx90640ImageP0, jfloatArray jmlx90640ImageP1,
                                jfloatArray jmlx90640ToP0, jfloatArray jmlx90640ToP1)
{
    int i;

    float averageTo = 0.0f;
    float maximumTo = -100.0f;
    float minimumTo =  100.0f;

    float chessGrid;

    if (jmlx90640ImageP0 && env->GetArrayLength(jmlx90640ImageP0) < ARRAY_LENGTH(mlx90640ImageP0)) {
        return -1;
    }
    if (jmlx90640ImageP1 && env->GetArrayLength(jmlx90640ImageP1) < ARRAY_LENGTH(mlx90640ImageP1)) {
        return -2;
    }
    if (jmlx90640ToP0 && env->GetArrayLength(jmlx90640ToP0) < ARRAY_LENGTH(mlx90640ToP0)) {
        return -3;
    }
    if (jmlx90640ToP1 && env->GetArrayLength(jmlx90640ToP1) < ARRAY_LENGTH(mlx90640ToP1)) {
        return -4;
    }

    for (i = 0; i < 2; i++)
    {
        /* Get one subpage
         */
        if (MLX90640_GetFrameData(frameData_40) < 0)
        {
            LOGD("mlx90640 get frame data failed\n");
            continue;
        }

        if (tr == 0)
        {
            tr = MLX90640_GetTa(frameData_40, &mlx90640);
            LOGD("==> Ambient temperature is %f\n", tr);
            tr = tr - TA_SHIFT;
            LOGD("==> Reflected temperature is %f\n", tr);
        }

        LOGD("==> Sub page number is %d\n", MLX90640_GetSubPageNumber(frameData_40));
        if (MLX90640_GetSubPageNumber(frameData_40) & 0x01)
        {
            /* Get image without calculate the temperature and ONLY for hot image
             */
            if (jmlx90640ImageP1)
                MLX90640_GetImage(frameData_40, &mlx90640, mlx90640ImageP1);

            /* Calculate actual object temparature if required
             */
            if (jmlx90640ToP1)
                MLX90640_CalculateTo(frameData_40, &mlx90640, emissivity, tr, mlx90640ToP1);
        }
        else
        {
            /* Get image without calculate the temperature and ONLY for hot image
             */
            if (jmlx90640ImageP0)
                MLX90640_GetImage(frameData_40, &mlx90640, mlx90640ImageP0);

            /* Calculate actual object temparature if required
             */
            if (jmlx90640ToP0)
                MLX90640_CalculateTo(frameData_40, &mlx90640, emissivity, tr, mlx90640ToP0);
        }
    }

    LOGD("\n\n");
    LOGD("==> IR Image Data[768]\n");
    for (i = 0; i < 768; i++) {
        chessGrid = mlx90640ImageP0[i]+mlx90640ImageP1[i];
#if DUMP_IMAGE
        LOGD("%f ", chessGrid);
#endif
    }

    LOGD("\n\n");
    LOGD("==> IR To Data[768]\n");
    for (i = 0; i < 768; i++) {
        chessGrid = mlx90640ToP0[i]+mlx90640ToP1[i];
        averageTo += chessGrid;
        if (minimumTo > chessGrid)
            minimumTo = chessGrid;
        if (maximumTo < chessGrid)
            maximumTo = chessGrid;
#if DUMP_TEMP
        LOGD("%f ", chessGrid);
#endif
    }

    LOGD("\n\n");
    LOGD("==> To min=%f max=%f average=%f\n", minimumTo, maximumTo, averageTo/768);


    if (jmlx90640ImageP0)
        env->SetFloatArrayRegion(jmlx90640ImageP0, 0, ARRAY_LENGTH(mlx90640ImageP0), mlx90640ImageP0);
    if (jmlx90640ImageP1)
        env->SetFloatArrayRegion(jmlx90640ImageP1, 0, ARRAY_LENGTH(mlx90640ImageP1), mlx90640ImageP1);
    if (jmlx90640ToP0)
        env->SetFloatArrayRegion(jmlx90640ToP0, 0, ARRAY_LENGTH(mlx90640ToP0), mlx90640ToP0);
    if (jmlx90640ToP1)
        env->SetFloatArrayRegion(jmlx90640ToP1, 0, ARRAY_LENGTH(mlx90640ToP1), mlx90640ToP1);

    return 0;
}

jint MLX90614_Measure(JNIEnv *env, jobject thiz, jfloatArray jtemp) {
    float temp = 0.0f;
    int ret = MLX90614_GetTo(&temp);
    env->SetFloatArrayRegion(jtemp, 0, 1, &temp);
    return ret;
}