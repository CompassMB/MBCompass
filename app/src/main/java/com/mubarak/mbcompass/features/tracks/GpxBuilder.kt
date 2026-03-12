package com.mubarak.mbcompass.features.tracks

import com.mubarak.mbcompass.features.tracks.model.Track
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GpxBuilder {


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


