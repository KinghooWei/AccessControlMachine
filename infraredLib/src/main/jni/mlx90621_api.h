/*
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
#ifndef _MLX90621_API_H_
#define _MLX90621_API_H_

#ifdef __cplusplus
extern "C" {
#endif

#define SCALEALPHA 0.000001

#define TA_SHIFT    8   /* the default shift for a MLX90640 device in open air */

typedef struct {
    int16_t vTh25;
    float kT1;
    float kT2;
    float tgc;
    float emissivity;
    float KsTa;
    float ksTo;
    float alpha[64];
    float ai[64];
    float bi[64];
    float cpAlpha;
    float cpA;
    float cpB;
    uint16_t brokenPixels[5];
    uint16_t outlierPixels[5];
} paramsMLX90621;

/*
 * Power on reset init step 1: Read the EEPROM table into RAM for fast access
 *
 * Return value: ret >= 0 for success, ret < 0 for failure
 */
int MLX90621_DumpEE(uint8_t *eeData);

/*
 * Power on reset init step 2: Write the oscillator trim and configuration value
 *
 * Return value: ret >= 0 for success, ret < 0 for failure
 */
int MLX90621_Configure(uint8_t *eeData);

/*
 * Power on reset init step 3: Extract the parameters from the EEPROM.
 * After the parameters are extracted, the EEPROM data is not needed anymore actually.
 */
void MLX90621_ExtractParameters(uint8_t *eeData, paramsMLX90621 *mlx90621);

/*
 * Data acquisition funtion: Read the frame data of a MLX90621 device
 * This function reads all the necessary frame data from a MLX90621 into a MCU memory location defined by
 * the user. The allocated memory should be at least 66 words for proper operation.
 *
 * Return value: ret >= 0 for success, ret < 0 for failure
 */
int MLX90621_GetFrameData(uint16_t *frameData);

/*
 * Calculation ambient temperature and return Ta measured from frame data and extracted parameters
 * @frameData – pointer to the MCU memory location where the frame data is stored
 * @params – pointer to the MCU memory location where the already extracted parameters for the MLX90621 device are stored
 */
float MLX90621_GetTa(uint16_t *frameData, const paramsMLX90621 *params);

/*
 * Calculates the object temperatures for all 64 pixels in the frame all based on the frame data
 * read from a MLX90621 device, the extracted parameters for that particular device and the emissivity defined
 * by the user. The allocated memory should be at least 64 words for proper operation.
 *
 * @frameData – pointer to the MCU memory location where the frame data is stored
 * @params – pointer to the MCU memory location where the already extracted parameters for the MLX90621 device are stored
 * @emissivity – emissivity defined by the user. The emissivity is a property of the measured object
 * @tr – reflected temperature defined by the user. If the object emissivity is less than 1, there might be some
 *  temperature reflected from the object. In order for this to be compensated the user should input this
 *  reflected temperature. The sensor ambient temperature could be used, but some shift depending on the
 *  enclosure might be needed. For a MLX90621 in the open air the shift is 0°C.
 * @result – pointer to the MCU memory location where the user wants the object temperatures data to be stored
 */
void MLX90621_CalculateTo(uint16_t *frameData, const paramsMLX90621 *params, float *result);
void MLX90621_CalculateTo_2(uint16_t *frameData, const paramsMLX90621 *params, float emissivity, float tr, float *result);

/*
 * Calculates values for all 64 pixels in the frame all based on the frame data read from a
 * MLX90621 device and the extracted parameters for that particular device. The allocated memory should be
 * at least 64 words for proper operation. The smaller the value, the lower the temperature in the pixels field
 * of view. Note that these are signed values.
 *
 * @frameData – pointer to the MCU memory location where the frame data is stored
 * @params – pointer to the MCU memory location where the already extracted parameters for the MLX90621 device are stored
 * @result – pointer to the MCU memory location where the user wants the object temperatures data to be stored
 */
void MLX90621_GetImage(uint16_t *frameData, const paramsMLX90621 *params, float *result);

/*
 * Configuration register Bit[10]=0 means POR or Brown-out occurred - Need to reload Configuration register
 *
 * Return value: ret = 0 no reload needed, ret = 1 POR reload needed
 */
int MLX90621_CheckReloadStatus();

int MLX90621_GetConfiguration(uint16_t *cfgReg);
int MLX90621_GetOscillatorTrim(uint16_t *oscTrim);

/* This function returns the current resolution of a MLX90621 device.
 * Configuration register Bit[5:4] 00=15-bit 01=16-bit 10=17-bit 11=18-bit(default)
 */
int MLX90621_GetCurResolution();

/*
 * Writes the desired resolution value (0x00 to 0x03) in the appropriate register in order to
 * change the current resolution of a MLX90621 device.
 * The return value is 0 if the write was successful, -1 if NACK occurred.
 */
int MLX90621_SetResolution(uint8_t resolution);

/* This function returns the current refresh rate of a MLX90621 device.
 * Configuration register Bit[3:0] 0000~0101=512Hz 0110~1110=256Hz~1Hz(default) 1111=0.5Hz
 */
int MLX90621_GetRefreshRate();

/*
 * Writes the desired refresh rate value in the appropriate register in order to
 * change the current refresh rate of a MLX90621 device.
 * The return value is 0 if the write was successful, -1 if NACK occurred.
 *
 * @refreshRate: 0x00~0x05=512Hz 0x06~0x0e=256Hz~1Hz(default) 0x0f=0.5Hz
 */
int MLX90621_SetRefreshRate(uint8_t refreshRate);


#ifdef __cplusplus
}
#endif
#endif /* _MLX90621_API_H_ */
