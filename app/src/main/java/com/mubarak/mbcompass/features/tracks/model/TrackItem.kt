// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.features.tracks.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Serializable
@Parcelize
data class TrackItem(
    val trackId: Long,
    var name: String,
    val date: Long,
    val length: Float,
    val duration: Long,
    val trackUriString: String,
    val gpxUriString: String,
    var starred: Boolean = false
) : Parcelable


@Serializable
@Parcelize
data class Tracklist(
    val trackItemList: MutableList<TrackItem> = mutableListOf(),
    var modificationDate: Long = System.currentTimeMillis(),
    var totalDistanceAll: Float = 0f
) : Parcelable {

    fun isEmpty(): Boolean = trackItemList.isEmpty()

}
