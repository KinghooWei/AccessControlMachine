/*
 * mlx90614 android i2c utitity porting by <lk@lztek.cn>
 *
 * Copyright (C) 2020 Shenzhen Lztek Ltd.
 */

#ifndef _MLX90614_API_H_
#define _MLX90614_API_H_

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Read Tobj1 object temperature and return 0 if success
 */
int MLX90614_GetTo(float *temp);


#ifdef __cplusplus
}
#endif

#endif