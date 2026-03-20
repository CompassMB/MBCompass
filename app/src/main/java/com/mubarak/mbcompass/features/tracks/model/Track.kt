package com.mubarak.mbcompass.features.tracks.model

import android.os.Parcelable
import com.mubarak.mbcompass.features.tracks.TrackingConstants.DEFAULT_LATITUDE
import com.mubarak.mbcompass.features.tracks.TrackingConstants.DEFAULT_LONGITUDE
import com.mubarak.mbcompass.features.tracks.TrackingConstants.DEFAULT_ZOOM_LEVEL
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Track(
    var length: Float = 0f,
    val wayPoints: MutableList<WayPoint> = mutableListOf(),
    var duration: Long = 0L,
    var durationOfPause: Long = 0L,
    var stepCount: Float = 0f,
    var recordingStart: Long = System.currentTimeMillis(),
    var recordingStop: Long = recordingStart,
    var maxAltitude: Double = 0.0,
    var minAltitude: Double = 0.0,
    var positiveElevation: Double = 0.0,
    var negativeElevation: Double = 0.0,
    var trackUriString: String = "",
    var gpxUriString: String = "",
    var name: String = "",
    var latitude: Double = DEFAULT_LATITUDE,
    var longitude: Double = DEFAULT_LONGITUDE,
    var zoomLevel: Double = DEFAULT_ZOOM_LEVEL
) : Parcelable {

    fun getTrackId(): Long = recordingStart

}