// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.sensor

import android.hardware.SensorManager
import android.location.Location
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import com.mubarak.mbcompass.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SensorStatusIconState(
    @DrawableRes val iconResId: Int,
    val contentDescription: String,
    val accuracy: Int? = null
)

data class AccuracyDialogState(
    val show: Boolean,
    val accuracyForDialog: Int? = null
)

class SensorViewModel : ViewModel() {

    var _trueNorthEnabled = MutableStateFlow(false)
    val trueNorthEnabled: StateFlow<Boolean> = _trueNorthEnabled.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    // StateFlow for the dynamic sensor status icon
    private val _sensorStatusIcon = MutableStateFlow(
        SensorStatusIconState(R.drawable.outline_question_mark_24, "Sensor status unknown")
    )
    val sensorStatusIcon: StateFlow<SensorStatusIconState> = _sensorStatusIcon.asStateFlow()

    private val _accuracyAlertDialogState = MutableStateFlow(AccuracyDialogState(show = false))
    val accuracyAlertDialogState: StateFlow<AccuracyDialogState> =
        _accuracyAlertDialogState.asStateFlow()

    private var autoDialogShownForCurrentLowState: Boolean = false

    fun updateSensorAccuracy(accuracy: Int) {
        val newIconState = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                autoDialogShownForCurrentLowState = false // Reset when accuracy is good
                SensorStatusIconState(
                    R.drawable.signal_full_24x,
                    "Sensor accuracy high",
                    accuracy
                )
            }

            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                autoDialogShownForCurrentLowState = false
                SensorStatusIconState(
                    R.drawable.signal_medium_24x,
                    "Sensor accuracy medium",
                    accuracy
                )
            }

            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                SensorStatusIconState(
                    R.drawable.signal_low_24x, "Sensor accuracy low", accuracy
                )
            }

            SensorManager.SENSOR_STATUS_UNRELIABLE,
            SensorManager.SENSOR_STATUS_NO_CONTACT -> {
                SensorStatusIconState(
                    R.drawable.signal_off_24x, "Sensor accuracy unreliable or no contact", accuracy
                )
            }

            else -> {
                SensorStatusIconState(
                    R.drawable.outline_question_mark_24, "Sensor status unknown", accuracy
                )
            }
        }
        _sensorStatusIcon.value = newIconState

        // Automatically show dialog ONCE when accuracy first becomes low/unreliable
        if ((accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW || accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) &&
            !autoDialogShownForCurrentLowState && !_accuracyAlertDialogState.value.show
        ) {
            _accuracyAlertDialogState.value =
                AccuracyDialogState(show = true, accuracyForDialog = accuracy)
            autoDialogShownForCurrentLowState = true
        }
    }

    fun provideLocation(location: Location?) {
        location?.let {
            _location.value = it
        }
    }

    fun setTrueNorthState(boolean: Boolean){
        _trueNorthEnabled.value = boolean
    }

    fun sensorStatusIconClicked() {
        val currentAccuracy = _sensorStatusIcon.value.accuracy
        if (currentAccuracy != null) {
            _accuracyAlertDialogState.value =
                AccuracyDialogState(show = true, accuracyForDialog = currentAccuracy)
        }
    }

    fun accuracyDialogDismissed() {
        _accuracyAlertDialogState.value = AccuracyDialogState(show = false)
    }
}