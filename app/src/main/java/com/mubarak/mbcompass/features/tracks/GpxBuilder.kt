// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.features.tracks

import android.location.Location
import com.mubarak.mbcompass.core.location.LocationHelper
import com.mubarak.mbcompass.features.tracks.model.Track
import com.mubarak.mbcompass.features.tracks.model.WayPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

object GpxBuilder {


    fun addWayPointToTrack(
        track: Track,
        location: Location,
        accuracyMultiplier: Int,
        resumed: Boolean
    ): Pair<Boolean, Track> {

        var numberOfWayPoints: Int = track.wayPoints.size
        val previousLocation: Location?

        when {
            numberOfWayPoints == 0 -> {
                previousLocation = null
            }
            numberOfWayPoints == 1 && !LocationHelper.isFirstLocationPlausible(location, track) -> {
                previousLocation = null
                numberOfWayPoints = 0
                track.wayPoints.removeAt(0)
            }
            else -> {
                previousLocation = track.wayPoints[numberOfWayPoints - 1].toLocation()
            }
        }

        // update duration on every call continuous accumulation
        val now: Long = GregorianCalendar.getInstance().time.time
        val difference: Long = now - track.recordingStop
        track.duration = track.duration + difference
        track.recordingStop = now

        val shouldBeAdded: Boolean = (
                LocationHelper.isRecent(location) &&
                        LocationHelper.isAccurate(location, TrackingConstants.LOCATION_ACCURACY) &&
                        LocationHelper.isDifferentEnough(previousLocation, location, accuracyMultiplier)
                )

        if (shouldBeAdded) {
            if (!resumed) {
                track.length += LocationHelper.calculateDistance(previousLocation, location)
            }

            val altitude: Double = location.altitude
            if (altitude != TrackingConstants.DEFAULT_ALTITUDE) {
                if (numberOfWayPoints == 0) {
                    track.maxAltitude = altitude
                    track.minAltitude = altitude
                } else {
                    if (altitude > track.maxAltitude) track.maxAltitude = altitude
                    if (altitude < track.minAltitude) track.minAltitude = altitude
                }
            }

            if (track.wayPoints.isNotEmpty()) {
                track.wayPoints[track.wayPoints.size - 1].isStopOver =
                    LocationHelper.isStopOver(previousLocation, location)
            }

            track.latitude = location.latitude
            track.longitude = location.longitude

            track.wayPoints.add(
                WayPoint(
                    location = location,
                    distanceToStart = track.length,
                )
            )
        }

        return Pair(shouldBeAdded, track)
    }

    // Calculates how long the recording has been paused since last stop.
    fun calculateDurationOfPause(recordingStop: Long): Long {
        return GregorianCalendar.getInstance().time.time - recordingStop
    }


    // Create GPX string from track
    fun createGpxString(track: Track): String {
        val sb = kotlin.text.StringBuilder()

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"MBCompass App (Android)\"\n")
        sb.append("     xmlns=\"http://www.topografix.com/GPX/1/1\"\n")
        sb.append("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
        sb.append("     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n")

        sb.append("\t<metadata>\n")
        sb.append("\t\t<name>MBCompass Recording: ${track.name}</name>\n")
        sb.append("\t</metadata>\n")

        // Starred points as waypoints
        track.wayPoints.filter { it.starred }.forEach { waypoint ->
            sb.append("\t<wpt lat=\"${waypoint.latitude}\" lon=\"${waypoint.longitude}\">\n")
            sb.append("\t\t<name>Point of Interest</name>\n")
            sb.append("\t\t<ele>${waypoint.altitude}</ele>\n")
            sb.append("\t</wpt>\n")
        }

        // Track segment
        sb.append("\t<trk>\n")
        sb.append("\t\t<name>Track</name>\n")
        sb.append("\t\t<trkseg>\n")

        track.wayPoints.forEach { waypoint ->
            sb.append("\t\t\t<trkpt lat=\"${waypoint.latitude}\" lon=\"${waypoint.longitude}\">\n")
            sb.append("\t\t\t\t<ele>${waypoint.altitude}</ele>\n")
            sb.append("\t\t\t\t<time>${formatGpxTime(waypoint.time)}</time>\n")
            sb.append("\t\t\t</trkpt>\n")
        }

        sb.append("\t\t</trkseg>\n")
        sb.append("\t</trk>\n")
        sb.append("</gpx>\n")

        return sb.toString()
    }

    private fun formatGpxTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }
}


