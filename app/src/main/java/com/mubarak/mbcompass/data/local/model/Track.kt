package com.mubarak.mbcompass.data.local.model

import android.location.Location
import android.os.Parcelable
import com.mubarak.mbcompass.utils.TrackingConstants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.collections.firstOrNull

@Serializable
@Parcelize
data class Track(
    val trackFormatVersion: Int = TrackingConstants.CURRENT_TRACK_FORMAT_VERSION,
    val wayPoints: MutableList<WayPoint> = mutableListOf(),
    var length: Float = 0f,
    var duration: Long = 0L,
    var recordingPaused: Long = 0L,
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
    var latitude: Double = TrackingConstants.DEFAULT_LATITUDE,
    var longitude: Double = TrackingConstants.DEFAULT_LONGITUDE,
    var zoomLevel: Double = 16.0
) : Parcelable {

    fun getTrackId(): Long = recordingStart

    fun isEmpty(): Boolean = wayPoints.isEmpty()

    fun getStartLocation(): Location? = wayPoints.firstOrNull()?.toLocation()

    fun getEndLocation(): Location? = wayPoints.lastOrNull()?.toLocation()
}

@Serializable
@Parcelize
data class TrackMetadata(
    val trackId: Long,
    val name: String,
    val date: Long,
    val length: Float,
    val duration: Long,
    val trackUriString: String,
    val gpxUriString: String,
    val starred: Boolean = false
) : Parcelable {

    companion object {
        fun fromTrack(track: Track): TrackMetadata {
            return TrackMetadata(
                trackId = track.getTrackId(),
                name = track.name,
                date = track.recordingStart,
                length = track.length,
                duration = track.duration,
                trackUriString = track.trackUriString,
                gpxUriString = track.gpxUriString,
                starred = track.wayPoints.any { it.starred }
            )
        }
    }
}
