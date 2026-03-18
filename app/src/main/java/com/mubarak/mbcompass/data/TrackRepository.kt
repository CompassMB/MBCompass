package com.mubarak.mbcompass.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.mubarak.mbcompass.features.tracks.GpxBuilder
import com.mubarak.mbcompass.features.tracks.model.Track
import com.mubarak.mbcompass.features.tracks.model.Tracklist
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Date
import java.util.GregorianCalendar
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TrackRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "TrackRepository"
        private const val TRACKLIST_FILE = "tracklist.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    // save track as JSON to storage.
    fun saveTrack(track: Track) {
        val jsonString = encodeTrack(track)
        if (jsonString.isNotBlank()) {
            writeTxtFile(jsonString, track.trackUriString.toUri())
        }
        val gpx = GpxBuilder.createGpxString(track)
        if (gpx.isNotBlank()) {
            writeTxtFile(gpx, track.gpxUriString.toUri())
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


    // Read the tracklist from storage. Returns an empty Tracklist on any failure
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


    // Save tracklist to storage.
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


    // Delete track files and remove entry from tracklist.
    fun deleteTrack(trackId: Long): Tracklist {
        val tracklist = readTracklist()
        val element = tracklist.trackItemList.find { it.trackId == trackId }
        element?.let {
            it.trackUriString.toUri().toFile().delete()
            it.gpxUriString.toUri().toFile().delete()
            tracklist.trackItemList.removeIf { e -> e.trackId == trackId }
            tracklist.totalDistanceAll -= it.length
            saveTracklist(tracklist)
        }
        return tracklist
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
}
