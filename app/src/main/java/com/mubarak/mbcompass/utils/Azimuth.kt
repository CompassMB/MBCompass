// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.utils

import kotlin.math.roundToInt

class Azimuth(rawDegrees: Float) {

    init {
        if (!rawDegrees.isFinite()) {
            throw IllegalArgumentException("Azimuth should be finite $rawDegrees")
        }
    }

    val degrees = wrapAzimuth(rawDegrees)

    val roundedDegrees = wrapAzimuth(rawDegrees.roundToInt().toFloat())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Azimuth

        return degrees == other.degrees
    }

    override fun hashCode(): Int {
        return degrees.hashCode()
    }

    fun add(degrees: Float) = Azimuth(this.degrees + degrees)

    fun wrapAzimuth(angleInDegrees: Float): Float {
        return (angleInDegrees + 360f) % 360f
    }
}
