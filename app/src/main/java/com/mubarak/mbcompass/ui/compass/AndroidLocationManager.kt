package com.mubarak.mbcompass.ui.compass

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
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import java.util.concurrent.Executor

const val TAG = "locationManager_debug"

class AndroidLocationManager(
    val context: Context, private val location: (location: Location) -> Unit
) {

    private var locationManager: LocationManager? = null
    private var locationRequestCancellationSignal: CancellationSignal? = null


    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun registerLocationListener() {
        locationManager = locationManager
            ?: context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

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


    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
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


    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun requestLocation(locationManager: LocationManager, provider: String) {
        Log.i(TAG, "Requesting location from provider '$provider'")

        locationRequestCancellationSignal?.cancel()
        locationRequestCancellationSignal = CancellationSignal()

        LocationManagerCompat.getCurrentLocation(
            locationManager,
            provider,
            locationRequestCancellationSignal,
            getExecutor(),
            ::setLocation
        )
    }


    private fun getExecutor(): Executor = ContextCompat.getMainExecutor(context)


    private fun getPreferredProviders(): List<String> {
        val preferredProviders = mutableListOf<String>()

        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            preferredProviders.add(LocationManager.FUSED_PROVIDER)
        }

        if (ContextCompat.checkSelfPermission(
                context,
                ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            preferredProviders.add(LocationManager.GPS_PROVIDER)
        }

        if (ContextCompat.checkSelfPermission(
                context,
                ACCESS_COARSE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            preferredProviders.add(LocationManager.NETWORK_PROVIDER)
        }

        return preferredProviders
    }


    private fun setLocation(location: Location?) {
        Log.i(TAG, "Location is null: $location")
        location?.let {
            location(it)
            Log.i(TAG, "Got a Location: $location")
        }
    }

}