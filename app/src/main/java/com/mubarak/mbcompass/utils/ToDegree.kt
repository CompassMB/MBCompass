// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.utils

object ToDegree {

    fun radiansToDegrees360(azimuth: Float): Float {
        return (Math.toDegrees(azimuth.toDouble()).toFloat() + 360) % 360
    }

}