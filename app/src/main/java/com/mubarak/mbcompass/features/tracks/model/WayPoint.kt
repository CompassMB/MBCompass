// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.features.tracks.model

import android.location.Location
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class WayPoint(
    val locationProvider: String,
    val time: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val distanceToStart: Float = 0f,
    var isStopOver: Boolean = false,
    var starred: Boolean = false
) : Parcelable {


    constructor(location: Location, distanceToStart: Float) : this(
        locationProvider = location.provider ?: "unknown",
        latitude = location.latitude,
        longitude = location.longitude,
        altitude = location.altitude,
        accuracy = location.accuracy,
        time = location.time,
        distanceToStart = distanceToStart,
    )

    fun toLocation(): Location {
        return Location(locationProvider).apply {
            latitude = this@WayPoint.latitude
            longitude = this@WayPoint.longitude
            altitude = this@WayPoint.altitude
            accuracy = this@WayPoint.accuracy
            time = this@WayPoint.time
        }
    }
}

