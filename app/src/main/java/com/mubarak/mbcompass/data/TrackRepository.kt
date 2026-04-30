// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.data

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.mubarak.mbcompass.features.tracks.GpxBuilder
import com.mubarak.mbcompass.features.tracks.model.Track
import com.mubarak.mbcompass.features.tracks.model.TrackItem
import com.mubarak.mbcompass.features.tracks.model.Tracklist
import com.mubarak.mbcompass.utils.DateTimeFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Date
import java.util.GregorianCalendar
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TrackRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "TrackRepository"
        private const val FOLDER_TRACKS = "tracks"
        private const val FOLDER_GPX = "gpx"
        private const val FOLDER_TEMP = "temp"
        private const val TEMP_FILE = "temp.json"
        private const val TRACKLIST_FILE = "tracklist.json"
        private const val GPX_EXTENSION = ".gpx"
        private const val TRACK_EXTENSION = ".json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }


    fun saveTrackAndUpdateTrack(track: Track): Track {
        val updatedTrack = track.copy(
            trackUriString = getTrackFileUri(track).toString(),
            gpxUriString = getGpxFileUri(track).toString()
        )
        saveTrack(updatedTrack)
        addTrackToTracklist(updatedTrack)
        return updatedTrack
    }


    fun saveTrack(track: Track) {
        // Save JSON
        val jsonString = encodeTrack(track)
        if (jsonString.isNotBlank()) {
            writeTxtFile(jsonString, track.trackUriString.toUri())
        }

        // Save GPX
        val gpx = GpxBuilder.createGpxString(track)
        if (gpx.isNotBlank()) {
            writeTxtFile(gpx, track.gpxUriString.toUri())
        }
    }


    fun saveTempTrack(track: Track) {
        val jsonString = encodeTrack(track)
        if (jsonString.isNotBlank()) {
            writeTxtFile(jsonString, getTempFileUri())
        }
    }


    fun readTrackFromUri(fileUri: Uri): Track {
        Log.d(TAG, "Reading track from: $fileUri")

        val text = readTxtFile(fileUri)
        if (text.isEmpty()) {
            Log.w(TAG, "Empty or missing file at $fileUri")
            return Track()
        }

        return try {
            val track = json.decodeFromString<Track>(text)
            Log.d(TAG, "Parsed track: '${track.name}', ${track.wayPoints.size} waypoints")
            track
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse track JSON at $fileUri", e)
            Track()
        }
    }

    fun readTracklist(): Tracklist {
        val text = readTxtFile(getTracklistFileUri())
        if (text.isBlank()) return Tracklist()

        return try {
            json.decodeFromString<Tracklist>(text)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse tracklist JSON", e)
            Tracklist()
        }
    }


    fun saveTracklist(
        tracklist: Tracklist,
        modificationDate: Date = GregorianCalendar.getInstance().time
    ) {
        val updated = tracklist.copy(modificationDate = modificationDate.time)

        val jsonString = try {
            json.encodeToString(updated)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode tracklist", e)
            return
        }

        writeTxtFile(jsonString, getTracklistFileUri())
    }

    private fun addTrackToTracklist(track: Track) {
        val tracklist = readTracklist()

        tracklist.trackItemList.add(
            TrackItem(
                trackId = track.getTrackId(),
                name = track.name,
                date = track.recordingStart,
                length = track.length,
                duration = track.duration,
                trackUriString = track.trackUriString,
                gpxUriString = track.gpxUriString
            )
        )

        tracklist.totalDistanceAll += track.length
        saveTracklist(tracklist)
    }


    fun deleteTrack(trackId: Long): Tracklist {
        val tracklist = readTracklist()
        val trackToDelete = tracklist.trackItemList.find { it.trackId == trackId }

        trackToDelete?.let { track ->
            track.trackUriString.toUri().toFile().delete()
            track.gpxUriString.toUri().toFile().delete()

            removeTrackFromList(tracklist.trackItemList, trackId)

            tracklist.totalDistanceAll -= track.length

            saveTracklist(tracklist)

            Log.d(TAG, "Deleted track: ${track.name} (ID: $trackId)")
        }

        return tracklist
    }

    private fun removeTrackFromList(list: MutableList<TrackItem>, trackId: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.removeIf { it.trackId == trackId }
        } else {
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().trackId == trackId) {
                    iterator.remove()
                    break
                }
            }
        }
    }

    fun deleteTempFile() {
        val tempFile = getTempFileUri().toFile()
        if (tempFile.exists()) {
            val deleted = tempFile.delete()
            if (deleted) {
                Log.d(TAG, "Deleted temp file")
            } else {
                Log.w(TAG, "Failed to delete temp file")
            }
        }
    }


    private fun getTrackFileUri(track: Track): Uri {
        val fileName = DateTimeFormatter.formatDateString(track.recordingStart) + TRACK_EXTENSION
        return File(context.getExternalFilesDir(FOLDER_TRACKS), fileName).toUri()
    }


    private fun getGpxFileUri(track: Track): Uri {
        return File(context.getExternalFilesDir(FOLDER_GPX), getGpxFileName(track)).toUri()
    }


    fun getGpxFileName(track: Track): String {
        return DateTimeFormatter.formatDateString(track.recordingStart) + GPX_EXTENSION
    }

    fun getTempFileUri(): Uri {
        return File(context.getExternalFilesDir(FOLDER_TEMP), TEMP_FILE).toUri()
    }


    private fun getTracklistFileUri(): Uri {
        return File(context.getExternalFilesDir(""), TRACKLIST_FILE).toUri()
    }


    private fun readTxtFile(fileUri: Uri): String {
        val file = fileUri.toFile()
        return if (file.exists()) {
            try {
                file.readText()
            } catch (e: Exception) {
                Log.e(TAG, "Error reading $fileUri", e)
                ""
            }
        } else {
            ""
        }
    }

    private fun writeTxtFile(text: String, fileUri: Uri) {
        if (text.isEmpty()) return

        try {
            val file = fileUri.toFile()
            file.parentFile?.mkdirs()
            file.writeText(text)
        } catch (e: Exception) {
            Log.e(TAG, "Error writing $fileUri", e)
        }
    }

    private fun encodeTrack(track: Track): String {
        return try {
            json.encodeToString(track)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode track '${track.name}'", e)
            ""
        }
    }
}