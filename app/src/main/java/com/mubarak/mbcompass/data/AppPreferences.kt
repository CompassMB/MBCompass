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
import android.location.Location
import android.location.LocationManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mubarak.mbcompass.features.tracks.TrackingConstants.DEFAULT_ACCURACY
import com.mubarak.mbcompass.features.tracks.TrackingConstants.DEFAULT_ALTITUDE
import com.mubarak.mbcompass.features.tracks.TrackingConstants.DEFAULT_LATITUDE
import com.mubarak.mbcompass.features.tracks.TrackingConstants.DEFAULT_LONGITUDE
import com.mubarak.mbcompass.features.tracks.TrackingConstants.DEFAULT_TIME
import com.mubarak.mbcompass.features.tracks.TrackingConstants.DEFAULT_ZOOM_LEVEL
import com.mubarak.mbcompass.features.tracks.TrackingConstants.STATE_TRACKING_NOT
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object AppPreferences {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    private lateinit var prefDataStore: DataStore<Preferences>

    // Preference keys
    private val TRACKING_STATE_KEY = intPreferencesKey("tracking_state")
    private val MAP_ZOOM_LEVEL_KEY = doublePreferencesKey("map_zoom_level")
    private val RECORDING_ACCURACY_HIGH_KEY = booleanPreferencesKey("recording_accuracy_high")

    // Current location keys
    private val CURRENT_LOCATION_PROVIDER_KEY = stringPreferencesKey("current_location_provider")
    private val CURRENT_LOCATION_LATITUDE_KEY = doublePreferencesKey("current_location_latitude")
    private val CURRENT_LOCATION_LONGITUDE_KEY = doublePreferencesKey("current_location_longitude")
    private val CURRENT_LOCATION_ACCURACY_KEY = floatPreferencesKey("current_location_accuracy")
    private val CURRENT_LOCATION_ALTITUDE_KEY = doublePreferencesKey("current_location_altitude")
    private val CURRENT_LOCATION_TIME_KEY = longPreferencesKey("current_location_time")


    // init it on Application class
    fun Context.initPreferences() {
        prefDataStore = this.dataStore
    }

    fun loadTrackingState(): Int = runBlocking {
        prefDataStore.data.first()[TRACKING_STATE_KEY] ?: STATE_TRACKING_NOT
    }

    fun saveTrackingState(trackingState: Int) = runBlocking {
        prefDataStore.edit { preferences ->
            preferences[TRACKING_STATE_KEY] = trackingState
        }
    }

    fun loadCurrentLocation(): Location {
        return runBlocking {
            val preferences = prefDataStore.data.first()
            val provider =
                preferences[CURRENT_LOCATION_PROVIDER_KEY] ?: LocationManager.NETWORK_PROVIDER

            Location(provider).apply {
                latitude = preferences[CURRENT_LOCATION_LATITUDE_KEY] ?: DEFAULT_LATITUDE
                longitude = preferences[CURRENT_LOCATION_LONGITUDE_KEY] ?: DEFAULT_LONGITUDE
                accuracy = preferences[CURRENT_LOCATION_ACCURACY_KEY] ?: DEFAULT_ACCURACY
                altitude = preferences[CURRENT_LOCATION_ALTITUDE_KEY] ?: DEFAULT_ALTITUDE
                time = preferences[CURRENT_LOCATION_TIME_KEY] ?: DEFAULT_TIME
            }
        }
    }


    fun saveCurrentLocation(location: Location) = runBlocking {
        prefDataStore.edit { preferences ->
            preferences[CURRENT_LOCATION_PROVIDER_KEY] =
                location.provider ?: LocationManager.NETWORK_PROVIDER
            preferences[CURRENT_LOCATION_LATITUDE_KEY] = location.latitude
            preferences[CURRENT_LOCATION_LONGITUDE_KEY] = location.longitude
            preferences[CURRENT_LOCATION_ACCURACY_KEY] = location.accuracy
            preferences[CURRENT_LOCATION_ALTITUDE_KEY] = location.altitude
            preferences[CURRENT_LOCATION_TIME_KEY] = location.time
        }
    }

    fun loadZoomLevel(): Double = runBlocking {
        prefDataStore.data.first()[MAP_ZOOM_LEVEL_KEY] ?: DEFAULT_ZOOM_LEVEL
    }


    fun saveZoomLevel(zoomLevel: Double) = runBlocking {
        prefDataStore.edit { preferences ->
            preferences[MAP_ZOOM_LEVEL_KEY] = zoomLevel
        }
    }


    fun loadAccuracyMultiplier(): Int = runBlocking {
        val recordingAccuracyHigh = prefDataStore.data.first()[RECORDING_ACCURACY_HIGH_KEY] ?: false
        if (recordingAccuracyHigh) 2 else 1
    }
}