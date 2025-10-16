// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass

import androidx.lifecycle.ViewModel
import com.mubarak.mbcompass.utils.Azimuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private var _azimuth = MutableStateFlow(Azimuth(0f))
    var azimuth: StateFlow<Azimuth> = _azimuth.asStateFlow()

    private val _strength = MutableStateFlow(0F)
    val strength: StateFlow<Float> = _strength.asStateFlow()

    fun updateAzimuth(azimuth: Azimuth) {
        _azimuth.value = azimuth
    }

    fun updateMagneticStrength(strengthInUt: Float) {
        _strength.value = strengthInUt
    }

}