// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.utils

object ToDegree {

    fun radiansToDegrees360(azimuth: Float): Float {
        return (Math.toDegrees(azimuth.toDouble()).toFloat() + 360) % 360
    }

}