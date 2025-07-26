// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mubarak.mbcompass.data.local.model.UserPreferences
import com.mubarak.mbcompass.utils.ThemeConfig
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

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        return UserPreferences(
            theme = preferences[PreferencesKeys.THEME] ?: ThemeConfig.FOLLOW_SYSTEM.prefName,
        )
    }

    private object PreferencesKeys {
        val THEME = stringPreferencesKey(UserPreferences.KEY_THEME)
    }
}