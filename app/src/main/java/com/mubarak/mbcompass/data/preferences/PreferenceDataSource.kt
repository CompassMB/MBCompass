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

interface PreferenceDataSource {

    val preferenceFlow: Flow<UserPreferences>

    suspend fun setOfflineMapFolder(key: String, value: String)
    suspend fun setMapSource(key: String, value: Boolean)
    suspend fun setTheme(key: String, value: String)
    suspend fun setTrueNorthValue(key: String, value: Boolean)
    suspend fun setTrueDarkValue(key: String, value: Boolean)
    suspend fun setHighAccuracy(value: Boolean)
    suspend fun saveMapState(latitude: Double, longitude: Double, zoomLevel: Double)
}