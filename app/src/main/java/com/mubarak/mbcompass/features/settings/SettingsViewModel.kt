// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.features.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubarak.mbcompass.data.preferences.UserPreferenceRepository
import com.mubarak.mbcompass.ui.theme.ThemeConfig
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
                useOfflineMaps = userPreferences.isOfflineMapSource,
                offlineMapFolder = userPreferences.offlineMapFolderPath,
                isTrueDarkThemeEnabled = userPreferences.isTrueDarkThemeEnabled,
                isTrueNorthEnabled = userPreferences.isTrueNorthEnabled
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

    fun setMapSourceState(isOfflineMapSource: Boolean){
        viewModelScope.launch {
            userPreferencesRepository.setMapSourceState(isOfflineMapSource)
        }
    }

    fun saveOfflineMapFolder(path: String){
        viewModelScope.launch {
            userPreferencesRepository.setOfflineMapFolder(path)
        }
    }
    fun setTrueDarkState(isTrueDarkThemeEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setTrueDarkState(isTrueDarkThemeEnabled)
        }
    }

    fun setTrueNorthState(isTrueNorthEnabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setTrueNorthState(isTrueNorthEnabled)
        }
    }

    data class SettingsUiState(
        val theme: String = ThemeConfig.FOLLOW_SYSTEM.prefName,
        val offlineMapFolder: String = "",
        val useOfflineMaps: Boolean = false,
        val isTrueNorthEnabled: Boolean = false,
        val isTrueDarkThemeEnabled: Boolean = false,
        val themeDialogOptions: List<String> = listOf(
            ThemeConfig.FOLLOW_SYSTEM.prefName,
            ThemeConfig.LIGHT.prefName,
            ThemeConfig.DARK.prefName,
        ),
    )
}