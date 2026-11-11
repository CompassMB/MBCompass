package com.mubarak.mbcompass.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AzimuthTest {

    @Test
    fun roundedDegrees_returnsIntegerWithoutPhonyDecimal() {
        val azimuth = Azimuth(67.4f)

        // Verifies the fix for Issue #201: azimuth is not formatted as "67.0"
        assertEquals(67, azimuth.roundedDegrees)
        assertEquals("67°", "${azimuth.roundedDegrees}°")
        assertNotEquals("67.0°", "${azimuth.roundedDegrees}°")
    }

    @Test
    fun degrees_isNotTruncatedForSmoothRotation() {
        // Verifies the fix for Issue #201: needle rotation can swing smoothly
        // between 67.4 and 67.5 without being truncated/rounded to 67 and 68.
        val az1 = Azimuth(67.4f)
        val az2 = Azimuth(67.5f)

        assertEquals(67.4f, az1.degrees, 0.001f)
        assertEquals(67.5f, az2.degrees, 0.001f)

        assertEquals(67, az1.roundedDegrees)
        assertEquals(68, az2.roundedDegrees)
    }

    @Test
    fun wrapAroundZeroAnd360() {
        val az0 = Azimuth(0f)
        assertEquals(0.0f, az0.degrees, 0.001f)
        assertEquals(0, az0.roundedDegrees)

        val az360 = Azimuth(360f)
        assertEquals(0.0f, az360.degrees, 0.001f)
        assertEquals(0, az360.roundedDegrees)

        val az3596 = Azimuth(359.6f)
        assertEquals(359.6f, az3596.degrees, 0.001f)
        assertEquals(0, az3596.roundedDegrees)

        val az3594 = Azimuth(359.4f)
        assertEquals(359.4f, az3594.degrees, 0.001f)
        assertEquals(359, az3594.roundedDegrees)
    }

    @Test
    fun negativeAngles_wrappedCorrectly() {
        val azNeg10 = Azimuth(-10f)
        assertEquals(350f, azNeg10.degrees, 0.001f)
        assertEquals(350, azNeg10.roundedDegrees)

        val azNeg370 = Azimuth(-370f)
        assertEquals(350f, azNeg370.degrees, 0.001f)
        assertEquals(350, azNeg370.roundedDegrees)
    }

    @Test
    fun add_wrapsCorrectly() {
        val az = Azimuth(350f).add(20f)
        assertEquals(10f, az.degrees, 0.001f)
        assertEquals(10, az.roundedDegrees)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonFinite_throwsException() {
        Azimuth(Float.NaN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun infinity_throwsException() {
        Azimuth(Float.POSITIVE_INFINITY)
    }
}
