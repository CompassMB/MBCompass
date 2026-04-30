// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mubarak.mbcompass.ui.theme.ThemeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferenceLocalDataStore @Inject constructor(context: Context) : PreferenceDataSource {

    private val dataStore: DataStore<Preferences> = context.dataStore

    override val preferenceFlow: Flow<UserPreferences>
        get() = dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                mapUserPreferences(preferences)
            }

    override suspend fun setValue(key: String, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    override suspend fun setTrueDarkValue(key: String, value: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    override suspend fun setHighAccuracy(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIGH_ACCURACY] = value
        }
    }

    override suspend fun saveMapState(
        latitude: Double,
        longitude: Double,
        zoomLevel: Double
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_LATITUDE] = latitude
            preferences[PreferencesKeys.LAST_LONGITUDE] = longitude
            preferences[PreferencesKeys.LAST_ZOOM_LEVEL] = zoomLevel
        }
    }

    override suspend fun setTrueNorthValue(key: String, value: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        return UserPreferences(
            theme = preferences[PreferencesKeys.THEME] ?: ThemeConfig.FOLLOW_SYSTEM.prefName,
            isTrueDarkThemeEnabled = preferences[PreferencesKeys.TRUE_DARK] ?: false,
            isTrueNorthEnabled = preferences[PreferencesKeys.TRUE_NORTH] ?: false,
            highAccuracy = preferences[PreferencesKeys.HIGH_ACCURACY] ?: false,
            lastLatitude = preferences[PreferencesKeys.LAST_LATITUDE] ?: 48.8583,
            lastLongitude = preferences[PreferencesKeys.LAST_LONGITUDE] ?: 2.2944,
            lastZoomLevel = preferences[PreferencesKeys.LAST_ZOOM_LEVEL] ?: 16.0
        )
    }

    private object PreferencesKeys {
        val THEME = stringPreferencesKey(UserPreferences.KEY_THEME)
        val TRUE_DARK = booleanPreferencesKey(UserPreferences.TRUE_DARK)
        val TRUE_NORTH = booleanPreferencesKey(UserPreferences.TRUE_NORTH)
        val HIGH_ACCURACY = booleanPreferencesKey(UserPreferences.HIGH_ACCURACY)
        val LAST_LATITUDE = doublePreferencesKey(UserPreferences.LAST_LATITUDE)
        val LAST_LONGITUDE = doublePreferencesKey(UserPreferences.LAST_LONGITUDE)
        val LAST_ZOOM_LEVEL = doublePreferencesKey(UserPreferences.LAST_ZOOM_LEVEL)
    }
}