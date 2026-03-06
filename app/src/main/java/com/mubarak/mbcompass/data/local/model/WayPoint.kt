package com.mubarak.mbcompass.data.local.model

import android.location.Location
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class WayPoint(
    val provider: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val time: Long,
    val distanceToStart: Float = 0f,
    val numberSatellites: Int = 0,
    var isStopOver: Boolean = false,
    var starred: Boolean = false
) : Parcelable {

    constructor(location: Location) : this(
        provider = location.provider ?: "unknown",
        latitude = location.latitude,
        longitude = location.longitude,
        altitude = location.altitude,
        accuracy = location.accuracy,
        time = location.time
    )

    constructor(location: Location, distanceToStart: Float, satellites: Int = 0) : this(
        provider = location.provider ?: "unknown",
        latitude = location.latitude,
        longitude = location.longitude,
        altitude = location.altitude,
        accuracy = location.accuracy,
        time = location.time,
        distanceToStart = distanceToStart,
        numberSatellites = satellites
    )

    fun toLocation(): Location {
        return Location(provider).apply {
            latitude = this@WayPoint.latitude
            longitude = this@WayPoint.longitude
            altitude = this@WayPoint.altitude
            accuracy = this@WayPoint.accuracy
            time = this@WayPoint.time
        }
    }
}

