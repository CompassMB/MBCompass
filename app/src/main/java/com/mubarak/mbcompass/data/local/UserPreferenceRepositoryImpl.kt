// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.data.local

import com.mubarak.mbcompass.data.local.model.UserPreferences
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepositoryImpl(
    private val dataStore: PreferenceDataSource,
) : UserPreferenceRepository {

    override val getUserPreferenceStream: Flow<UserPreferences>
        get() = dataStore.preferenceFlow

    override suspend fun setTheme(theme: String) {
        dataStore.setValue(UserPreferences.KEY_THEME, theme)
    }

    override suspend fun setTrueNorthState(boolean: Boolean) {
        dataStore.setValue(UserPreferences.TRUE_NORTH,boolean)
    }
}