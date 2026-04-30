// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */


package com.mubarak.mbcompass.utils

import kotlin.text.format

object LengthUnitHelper {

    fun convertDistanceToString(distance: Float): String =
        convertDistanceToString(distance.toDouble())

    fun convertDistanceToString(distance: Double): String {
        return if (distance >= 1_000.0) {
            "%.2f km".format(distance / 1_000.0)
        } else {
            "%.0f m".format(distance)
        }
    }

    fun convertMetersPerSecond(metersPerSecond: Double): Double {
        return metersPerSecond * 3.6
    }
}