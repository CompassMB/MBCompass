// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.data.preferences

import kotlinx.coroutines.flow.Flow

class UserPreferencesRepositoryImpl(
    private val dataStore: PreferenceDataSource,
) : UserPreferenceRepository {

    override val getUserPreferenceStream: Flow<UserPreferences>
        get() = dataStore.preferenceFlow

    override suspend fun setTheme(theme: String) {
        dataStore.setValue(UserPreferences.KEY_THEME, theme)
    }

    override suspend fun setTrueDarkState(boolean: Boolean) {
        dataStore.setTrueDarkValue(UserPreferences.TRUE_DARK,boolean)
    }

    override suspend fun setTrueNorthState(boolean: Boolean) {
        dataStore.setTrueNorthValue(UserPreferences.TRUE_NORTH,boolean)
    }

    override suspend fun setHighAccuracy(value: Boolean) {
        dataStore.setHighAccuracy(value)
    }

    override suspend fun saveMapState(
        latitude: Double,
        longitude: Double,
        zoomLevel: Double
    ) {
        dataStore.saveMapState(latitude, longitude, zoomLevel)
    }
}