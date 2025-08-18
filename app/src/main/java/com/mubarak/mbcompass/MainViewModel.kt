// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

class MainViewModel : ViewModel() {
    private val _azimuth = MutableStateFlow(0F)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _strength = MutableStateFlow(0F)
    val strength: StateFlow<Float> = _strength.asStateFlow()

    fun updateAzimuth(azimuth: Float) {
        _azimuth.value = azimuth
    }

    fun updateMagneticStrength(strengthInUt: Float) {
        _strength.value = strengthInUt
    }

}