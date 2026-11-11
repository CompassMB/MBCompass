// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.core.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.CancellationSignal
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import java.util.concurrent.Executor

const val TAG = "locationManager_debug"

class AndroidLocationManager(
    val context: Context, private val location: (location: Location) -> Unit
) {

    private var locationManager: LocationManager? = null
    private var locationRequestCancellationSignal: CancellationSignal? = null

    fun hasLocationPermission(): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            ACCESS_COARSE_LOCATION
        ) == PERMISSION_GRANTED
        return hasFine || hasCoarse
    }

    fun registerLocationListener() {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted, skipping registerLocationListener")
            setLocation(null)
            return
        }

        locationManager = locationManager
            ?: context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        if (locationManager == null) {
            Log.w(TAG, "LocationManager not present")
            setLocation(null)
            return
        }

        if (!LocationManagerCompat.isLocationEnabled(locationManager!!)) {
            Log.w(TAG, "Location is disabled")
            setLocation(null)
            return
        }

        requestLocation(locationManager!!)
    }

    private fun requestLocation(locationManager: LocationManager) {
        getBestLocationProvider(locationManager)
            ?.also { provider -> requestLocation(locationManager, provider) }
            ?: run {
                Log.w(TAG, "No LocationProvider available")
                setLocation(null)
            }
    }

    private fun getBestLocationProvider(locationManager: LocationManager): String? {
        val availableProviders = locationManager.getProviders(true)

        for (preferredProvider in getPreferredProviders()) {
            if (availableProviders.contains(preferredProvider)) {
                return preferredProvider
            }
        }

        return null
    }

    private fun requestLocation(locationManager: LocationManager, provider: String) {
        Log.i(TAG, "Requesting location from provider '$provider'")

        locationRequestCancellationSignal?.cancel()
        locationRequestCancellationSignal = CancellationSignal()

        try {
            LocationManagerCompat.getCurrentLocation(
                locationManager,
                provider,
                locationRequestCancellationSignal,
                getExecutor(),
                ::setLocation
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while requesting location from provider '$provider'", e)
            setLocation(null)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while requesting location from provider '$provider'", e)
            setLocation(null)
        }
    }

    private fun getExecutor(): Executor = ContextCompat.getMainExecutor(context)

    private fun getPreferredProviders(): List<String> {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            ACCESS_COARSE_LOCATION
        ) == PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return emptyList()
        }

        val locationProviders = mutableListOf<String>()

        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            locationProviders.add(LocationManager.FUSED_PROVIDER)
        }

        if (hasFine) {
            locationProviders.add(LocationManager.GPS_PROVIDER)
        }

        if (hasCoarse || hasFine) {
            locationProviders.add(LocationManager.NETWORK_PROVIDER)
        }

        return locationProviders
    }

    private fun setLocation(location: Location?) {
        Log.i(TAG, "Location is null: $location")
        location?.let {
            location(it)
            Log.i(TAG, "Got a Location: $location")
        }
    }
}