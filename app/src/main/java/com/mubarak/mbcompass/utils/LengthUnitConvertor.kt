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