// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.utils

import android.hardware.GeomagneticField
import android.location.Location
import androidx.annotation.StringRes
import com.mubarak.mbcompass.R

object CardinalDirection {

    fun getDirectionFromAzimuth(azimuth: Float) = when (azimuth) {
        in 22.5f..<67.5f -> DIRECTION.NORTHEAST
        in 67.5f..<112.5f -> DIRECTION.EAST
        in 112.5f..<157.5f -> DIRECTION.SOUTHEAST
        in 157.5f..<202.5f -> DIRECTION.SOUTH
        in 202.5f..<247.5f -> DIRECTION.SOUTHWEST
        in 247.5f..<292.5f -> DIRECTION.WEST
        in 292.5f..<337.5f -> DIRECTION.NORTHWEST
        else -> DIRECTION.NORTH
    }

}

fun getMagneticDeclination(location: Location): Float {
    val latitude = location.latitude.toFloat()
    val longitude = location.longitude.toFloat()
    val altitude = location.altitude.toFloat()
    val time = location.time
    // Based on WGS84 geodetic coordinates
    val geomagneticField = GeomagneticField(latitude, longitude, altitude, time)
    return geomagneticField.declination
}


enum class DIRECTION(@param:StringRes val dirName: Int) {
    NORTH(R.string.north),
    NORTHEAST(R.string.northeast),
    EAST(R.string.east),
    SOUTHEAST(R.string.southeast),
    SOUTH(R.string.south),
    SOUTHWEST(R.string.southwest),
    WEST(R.string.west),
    NORTHWEST(R.string.northwest),
}
