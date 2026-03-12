// SPDX-License-Identifier: GPL-3.0-or-later

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