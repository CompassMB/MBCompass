// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubarak.mbcompass.data.local.UserPreferenceRepository
import com.mubarak.mbcompass.utils.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferenceRepository,
) : ViewModel() {

    val uiState = userPreferencesRepository.getUserPreferenceStream
        .map { userPreferences ->
            SettingsUiState(
                theme = userPreferences.theme,
            )
        }.catch {
            Log.d("SettingsViewModel", "Error getting user preference", it)
            emit(SettingsUiState())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTheme(theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.setTheme(theme)
        }
    }

    data class SettingsUiState(
        val theme: String = ThemeConfig.FOLLOW_SYSTEM.prefName,
        val themeDialogOptions: List<String> = listOf(
            ThemeConfig.FOLLOW_SYSTEM.prefName,
            ThemeConfig.LIGHT.prefName,
            ThemeConfig.DARK.prefName,
        ),
    )
}