// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.data.local

import com.mubarak.mbcompass.data.local.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferenceRepository {

    val getUserPreferenceStream: Flow<UserPreferences>

    suspend fun setTheme(theme: String)
    suspend fun setTrueNorthState(boolean: Boolean)
}