package com.mubarak.mbcompass.data.local.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class TrackListModel(
    val trackId: Long,
    var name: String,
    val date: Long,
    val length: Float,
    val duration: Long,
    val trackUriString: String,
    val gpxUriString: String,
    var starred: Boolean = false
) : Parcelable

/**
 * Represents the full collection of saved tracks.
 */
@Serializable
@Parcelize
data class Tracklist(
    val tracklistElements: MutableList<TrackListModel> = mutableListOf(),
    var modificationDate: Long = System.currentTimeMillis(),
    var totalDistanceAll: Float = 0f
) : Parcelable {

    fun isEmpty(): Boolean = tracklistElements.isEmpty()

}
