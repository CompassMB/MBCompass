// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubarak.mbcompass.data.local.UserPreferenceRepository
import com.mubarak.mbcompass.utils.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HostViewModel @Inject constructor(
    userPreferencesRepository: UserPreferenceRepository,
) : ViewModel() {

    val uiStateFlow = userPreferencesRepository.getUserPreferenceStream
        .map { userPreferences ->
            UiState(
                isLoading = false,
                darkThemeConfig = when (userPreferences.theme) {
                    ThemeConfig.LIGHT.prefName -> ThemeConfig.LIGHT
                    ThemeConfig.DARK.prefName -> ThemeConfig.DARK
                    else -> ThemeConfig.FOLLOW_SYSTEM
                },
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            UiState()
        )

    data class UiState(
        val isLoading: Boolean = true, // this flag help to show the splash screen in the initial loading state
        val darkThemeConfig: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM,
    )
}



