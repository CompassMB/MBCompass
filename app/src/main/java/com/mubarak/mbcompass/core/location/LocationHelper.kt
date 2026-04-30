// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.mubarak.mbcompass.data.AppPreferences
import com.mubarak.mbcompass.features.tracks.TrackingConstants
import com.mubarak.mbcompass.features.tracks.model.Track
import com.mubarak.mbcompass.utils.LengthUnitHelper
import java.util.GregorianCalendar
import kotlin.math.pow


object LocationHelper {

    private const val TAG = "LocationHelper"

    //Check if location services (GPS/Network) are enabled
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManagerCompat.isLocationEnabled(locationManager)
        } else {
            try {
                val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                gpsEnabled || networkEnabled
            } catch (e: Exception) {
                false
            }
        }
    }

    fun getDefaultLocation(): Location {
        val defaultLocation = Location(LocationManager.NETWORK_PROVIDER)
        defaultLocation.latitude = TrackingConstants.DEFAULT_LATITUDE
        defaultLocation.longitude = TrackingConstants.DEFAULT_LONGITUDE
        defaultLocation.accuracy = TrackingConstants.DEFAULT_ACCURACY
        defaultLocation.altitude = TrackingConstants.DEFAULT_ALTITUDE
        defaultLocation.time = TrackingConstants.DEFAULT_TIME
        return defaultLocation
    }

    // return lastKnowLocation from system + app preference
    fun getLastKnownLocation(context: Context): Location {
        // This is our fallback - always available even if GPS is off
        var lastKnownLocation: Location = AppPreferences.loadCurrentLocation()

        Log.d(TAG, "getLastKnownLocation: $lastKnownLocation")

        // try to get more recent system locations
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // get GPS location (or fallback to saved location)
            val lastKnownLocationGps: Location =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lastKnownLocation

            // get Network location (or fallback to saved location)
            val lastKnownLocationNetwork: Location =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) ?: lastKnownLocation

            lastKnownLocation = when (isBestLocation(lastKnownLocationGps, lastKnownLocationNetwork)) {
                true -> lastKnownLocationGps
                false -> lastKnownLocationNetwork
            }
        }

        return lastKnownLocation
    }

    fun isStaleLocation(location: Location): Boolean {
        return GregorianCalendar.getInstance().time.time - location.time > TrackingConstants.SIGNIFICANT_TIME_DIFFERENCE
    }

    fun isBestLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            return true
        }

        val timeDelta: Long = location.time - currentBestLocation.time
        val isSignificantlyNewer: Boolean = timeDelta > TrackingConstants.SIGNIFICANT_TIME_DIFFERENCE
        val isSignificantlyOlder: Boolean = timeDelta < -TrackingConstants.SIGNIFICANT_TIME_DIFFERENCE

        when {
            isSignificantlyNewer -> return true
            isSignificantlyOlder -> return false
        }

        val isNewer: Boolean = timeDelta > 0L
        val accuracyDelta: Float = location.accuracy - currentBestLocation.accuracy
        val isLessAccurate: Boolean = accuracyDelta > 0f
        val isMoreAccurate: Boolean = accuracyDelta < 0f
        val isSignificantlyLessAccurate: Boolean = accuracyDelta > 200f

        val isFromSameProvider: Boolean = location.provider == currentBestLocation.provider

        return when {
            isMoreAccurate -> true
            isNewer && !isLessAccurate -> true
            isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
            else -> false
        }
    }

    fun isGpsEnabled(locationManager: LocationManager): Boolean {
        if (locationManager.allProviders.contains(LocationManager.GPS_PROVIDER)) {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
        return false
    }

    fun isNetworkEnabled(locationManager: LocationManager): Boolean {
        if (locationManager.allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
        return false
    }


    fun isRecent(location: Location): Boolean {
        val locationAge: Long = SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos
        return locationAge < TrackingConstants.DEFAULT_THRESHOLD_LOCATION_AGE
    }

    fun isAccurate(location: Location, locationAccuracyThreshold: Int): Boolean {
        return when (location.provider) {
            LocationManager.GPS_PROVIDER -> location.accuracy < locationAccuracyThreshold
            else -> location.accuracy < locationAccuracyThreshold + 10
        }
    }


    fun isFirstLocationPlausible(secondLocation: Location, track: Track): Boolean {
        val speed: Double = calculateSpeed(
            firstLocation = track.wayPoints[0].toLocation(),
            secondLocation = secondLocation,
            firstTimestamp = track.recordingStart,
            secondTimestamp = GregorianCalendar.getInstance().time.time
        )
        return speed < TrackingConstants.IMPLAUSIBLE_TRACK_START_SPEED
    }

    private fun calculateSpeed(
        firstLocation: Location,
        secondLocation: Location,
        firstTimestamp: Long,
        secondTimestamp: Long,
    ): Double {
        val timeDifference: Long = (secondTimestamp - firstTimestamp) / 1000L
        val distance = calculateDistance(firstLocation, secondLocation).toDouble()
        return LengthUnitHelper.convertMetersPerSecond(distance / timeDifference)
    }

    fun isDifferentEnough(
        previousLocation: Location?,
        location: Location,
        accuracyMultiplier: Int
    ): Boolean {
        if (previousLocation == null) return true

        val accuracy: Float = if (location.accuracy != 0.0f) {
            location.accuracy
        } else {
            TrackingConstants.DEFAULT_THRESHOLD_DISTANCE
        }

        val previousAccuracy: Float = if (previousLocation.accuracy != 0.0f) {
            previousLocation.accuracy
        } else {
            TrackingConstants.DEFAULT_THRESHOLD_DISTANCE
        }

        val accuracyDelta: Double = Math.sqrt((accuracy.pow(2) + previousAccuracy.pow(2)).toDouble())
        val distance: Float = calculateDistance(previousLocation, location)

        return distance > accuracyDelta * accuracyMultiplier
    }



     // Calculate distance between two points
    fun calculateDistance(previousLocation: Location?, location: Location): Float {
        var distance = 0f
        if (previousLocation != null) {
            distance = previousLocation.distanceTo(location)
        }
        return distance
    }

    fun calculateElevationDifferences(
        currentAltitude: Double,
        previousAltitude: Double,
        track: Track
    ): Track {
        if (currentAltitude != TrackingConstants.DEFAULT_ALTITUDE &&
            previousAltitude != TrackingConstants.DEFAULT_ALTITUDE) {

            val altitudeDifference: Double = currentAltitude - previousAltitude
            if (altitudeDifference > 0) {
                track.positiveElevation += altitudeDifference
            }
            if (altitudeDifference < 0) {
                track.negativeElevation += altitudeDifference
            }
        }
        return track
    }

    fun isStopOver(previousLocation: Location?, location: Location): Boolean {
        if (previousLocation == null) return false
        return location.time - previousLocation.time > TrackingConstants.STOP_OVER_THRESHOLD
    }
}