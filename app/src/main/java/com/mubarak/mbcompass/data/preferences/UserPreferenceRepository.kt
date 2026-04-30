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

interface UserPreferenceRepository {

    val getUserPreferenceStream: Flow<UserPreferences>

    suspend fun setTheme(theme: String)
    suspend fun setTrueDarkState(boolean: Boolean)
    suspend fun setTrueNorthState(boolean: Boolean)
    suspend fun setHighAccuracy(value: Boolean)
    suspend fun saveMapState(latitude: Double, longitude: Double, zoomLevel: Double)
}