package com.mubarak.mbcompass.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CardinalDirectionTest {

    @Test
    fun getDirectionFromAzimuth_floatInputs() {
        assertEquals(DIRECTION.NORTH, CardinalDirection.getDirectionFromAzimuth(0f))
        assertEquals(DIRECTION.NORTH, CardinalDirection.getDirectionFromAzimuth(22.4f))
        assertEquals(DIRECTION.NORTHEAST, CardinalDirection.getDirectionFromAzimuth(22.5f))
        assertEquals(DIRECTION.NORTHEAST, CardinalDirection.getDirectionFromAzimuth(67.4f))
        assertEquals(DIRECTION.EAST, CardinalDirection.getDirectionFromAzimuth(67.5f))
        assertEquals(DIRECTION.SOUTHEAST, CardinalDirection.getDirectionFromAzimuth(135f))
        assertEquals(DIRECTION.SOUTH, CardinalDirection.getDirectionFromAzimuth(180f))
        assertEquals(DIRECTION.SOUTHWEST, CardinalDirection.getDirectionFromAzimuth(225f))
        assertEquals(DIRECTION.WEST, CardinalDirection.getDirectionFromAzimuth(270f))
        assertEquals(DIRECTION.NORTHWEST, CardinalDirection.getDirectionFromAzimuth(315f))
        assertEquals(DIRECTION.NORTH, CardinalDirection.getDirectionFromAzimuth(350f))
    }

    @Test
    fun getDirectionFromAzimuth_intInputs() {
        assertEquals(DIRECTION.NORTH, CardinalDirection.getDirectionFromAzimuth(0))
        assertEquals(DIRECTION.NORTHEAST, CardinalDirection.getDirectionFromAzimuth(45))
        assertEquals(DIRECTION.EAST, CardinalDirection.getDirectionFromAzimuth(90))
        assertEquals(DIRECTION.SOUTHEAST, CardinalDirection.getDirectionFromAzimuth(135))
        assertEquals(DIRECTION.SOUTH, CardinalDirection.getDirectionFromAzimuth(180))
        assertEquals(DIRECTION.SOUTHWEST, CardinalDirection.getDirectionFromAzimuth(225))
        assertEquals(DIRECTION.WEST, CardinalDirection.getDirectionFromAzimuth(270))
        assertEquals(DIRECTION.NORTHWEST, CardinalDirection.getDirectionFromAzimuth(315))
        assertEquals(DIRECTION.NORTH, CardinalDirection.getDirectionFromAzimuth(360))
    }
}
