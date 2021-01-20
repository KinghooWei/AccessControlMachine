/**
 * @copyright (C) 2017 Melexis N.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#ifndef _MLX90640_API_H_
#define _MLX90640_API_H_

#ifdef __cplusplus
extern "C" {
#endif

#define SCALEALPHA 0.000001

#define TA_SHIFT    8   /* the default shift for a MLX90640 device in open air */

typedef struct {
    int16_t kVdd;
    int16_t vdd25;
    float KvPTAT;
    float KtPTAT;
    uint16_t vPTAT25;
    float alphaPTAT;
    int16_t gainEE;
    float tgc;
    float cpKv;
    float cpKta;
    uint8_t resolutionEE;
    uint8_t calibrationModeEE;
    float KsTa;
    float ksTo[5];
    int16_t ct[5];
    uint16_t alpha[768];
    uint8_t alphaScale;
    int16_t offset[768];
    int8_t kta[768];
    uint8_t ktaScale;
    int8_t kv[768];
    uint8_t kvScale;
    float cpAlpha[2];
    int16_t cpOffset[2];
    float ilChessC[3];
    uint16_t brokenPixels[5];
    uint16_t outlierPixels[5];
} paramsMLX90640;

/*
 * Reads all the necessary EEPROM data from a MLX90640 device with a given slave address into a
 * MCU memory location defined by the user. The allocated memory should be at least 832 words for proper
 * operation.
 *
 * Return value: ret = 0 for success, ret -1 for failure
 */
int MLX90640_DumpEE(uint16_t *eeData);

/*
 * Extracts the parameters from a given EEPROM data array and stores values as type defined in
 * mlx90640_api.h. After the parameters are extracted, the EEPROM data is not needed anymore and the
 * memory it was stored in could be reused. If the returned value is -7, the EEPROM data at the specified
 * location is not a valid MLX90640 EEPROM and the parameters extraction is aborted.
 */
int MLX90640_ExtractParameters(uint16_t *eeData, paramsMLX90640 *mlx90640);

/*
 * Reads all the necessary frame data from a MLX90640 device with a given slave address into a
 * MCU memory location defined by the user. The allocated memory should be at least 834 words for proper
 * operation.
 *
 * Return value: ret >= 0 for success, ret -1 for failure, ret = -8 for data could not be acquired for a certain time
 */
int MLX90640_GetFrameData(uint16_t *frameData);

/*
 * This function returns the current Vdd from a given MLX90640 frame data and extracted parameters.
 */
float MLX90640_GetVdd(uint16_t *frameData, const paramsMLX90640 *params);

/*
 * This function returns the current Ta measured in a given MLX90640 frame data and extracted parameters.
 */
float MLX90640_GetTa(uint16_t *frameData, const paramsMLX90640 *params);

/*
 * This function calculates values for all 768 pixels in the frame all based on the frame data read from a
 * MLX90640 device and the extracted parameters for that particular device. The allocated memory should be
 * at least 768 words for proper operation. The smaller the value, the lower the temperature in the pixels field
 * of view. Note that these are signed values.
 */
void MLX90640_GetImage(uint16_t *frameData, const paramsMLX90640 *params, float *result);

/*
 * This function calculates the object temperatures for all 768 pixel in the frame all based on the frame data
 * read from a MLX90640 device, the extracted parameters for that particular device and the emissivity defined
 * by the user. The allocated memory should be at least 768 words for proper operation.
 *
 * E.g. 1. Calculate the object temperatures for all the pixels in a frame, object emissivity is 0.95 and the
 * reflected temperature is 23.15°C (measured by the user).
 *
 * E.g. 2. Calculate the object temperatures for all the pixels in a frame, object emissivity is 0.95 and the
 * reflected temperature is the sensor ambient temperature with the default shift 8 in open air.
 *
 * Note: If absolute temperature values are not needed, the MLX90640_GetImage() function could be used
 * instead. In that case the MLX90640_CalculateTo() function should not be called.
 */
void MLX90640_CalculateTo(uint16_t *frameData, const paramsMLX90640 *params, float emissivity, float tr, float *result);

/*
 * Writes the desired resolution value (0x00 to 0x03) in the appropriate register in order to
 * change the current resolution of a MLX90640 device with a given slave address. Note that after power-on
 * reset, the resolution will revert back to the resolution stored in the EEPROM. The return value is 0 if the
 * write was successful, -1 if NACK occurred during the communication and -2 if the written value is not the
 * same as the intended one.
 */
int MLX90640_SetResolution(uint8_t resolution);

/*
 * Returns the current refresh rate of a MLX90640 device with a given slave address. Note that
 * the current refresh rate might differ from the one set in the EEPROM of that device.
 * If the result is -1, NACK occurred and this is not a valid refresh rate data.
 *
 * Return 0=16-bit 1=17-bit 2=18-bit (default) 3=19-bit
 */
int MLX90640_GetCurResolution();

/*
 * Writes the desired refresh rate value (0x00 to 0x07) in the appropriate register in order to
 * change the current refresh rate of a MLX90640 device with a given slave address. Note that after power -on
 * reset, the refresh rate will revert back to the refresh rate stored in the EEPROM. The return value is 0 if the
 * write was successful, -1 if NACK occurred during the communication and -2 if the written value is not the
 * same as the intended one.
 *
 * @refreshRate: 0x00=0.5Hz 0x01=1Hz 0x02=2Hz(default) 0x03/0x04/0x05/0x06/0x07=4Hz/8Hz/16Hz/32Hz/64Hz
 */
int MLX90640_SetRefreshRate(uint8_t refreshRate);

/*
 * Returns the current refresh rate of a MLX90640 device with a given slave address. Note that
 * the current refresh rate might differ from the one set in the EEPROM of that device.
 * If the result is -1, NACK occurred and this is not a valid refresh rate data.
 *
 * Return 0=0.5Hz 1=1Hz 2=2Hz(default) 3/4/5/6/7=4Hz/8Hz/16Hz/32Hz/64Hz
 */
int MLX90640_GetRefreshRate();

/*
 * This function returns the sub-page for a selected frame data of a MLX90640 device.
 * Return 0 for subpage 0, return 1 for subpage 1.
 */
int MLX90640_GetSubPageNumber(uint16_t *frameData);

/*
 * This function returns the working mode of a MLX90640 device.
 * Return 0 if interleaved mode is set, 1 if chess pattern mode is set.
 */
int MLX90640_GetCurMode();

/*
 * Sets to interleaved mode a MLX90640 device with a given slave address. Note that after power -
 * on reset, the mode will revert back to the one stored in the EEPROM. The return value is 0 if the write was
 * successful, -1 if NACK occurred during the communication and -2 if the written value is not the same as the
 * intended one.
 */
int MLX90640_SetInterleavedMode();

/*
 * Sets to chess pattern mode a MLX90640 device with a given slave address. Note that after
 * power-on reset, the mode will revert back to the one stored in the EEPROM. The return value is 0 if the
 * write was successful, -1 if NACK occurred during the communication and -2 if the written value is not the
 * same as the intended one.
 */
int MLX90640_SetChessMode();

/*
 * This function corrects the values of the broken pixels and/or the outlier pixels. The values of all pixels
 * indexes in the pixels array (until value 0xFFFF is read) will be corrected using different filtering methods. The
 * pixels that are marked as broken or outliers are already being reported in the paramsMLX90640 arrays
 * brokenPixels and outlierPixels. Note that it is possible to choose which pixels to be corrected by creating a
 * custom list with the indexes of those pixels. The list should end with 0xFFFF.
 */
void MLX90640_BadPixelsCorrection(uint16_t *pixels, float *to, int mode, paramsMLX90640 *params);

#ifdef __cplusplus
}
#endif

#endif
