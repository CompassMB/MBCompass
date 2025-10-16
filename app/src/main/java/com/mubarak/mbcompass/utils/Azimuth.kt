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
