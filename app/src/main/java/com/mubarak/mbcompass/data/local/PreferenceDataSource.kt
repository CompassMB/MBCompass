// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.data.local

import com.mubarak.mbcompass.data.local.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferenceDataSource {

    val preferenceFlow: Flow<UserPreferences>

    suspend fun setValue(key: String, value: String)
    suspend fun setTrueNorthValue(key: String, value: Boolean)
    suspend fun setTrueDarkValue(key: String, value: Boolean)
}