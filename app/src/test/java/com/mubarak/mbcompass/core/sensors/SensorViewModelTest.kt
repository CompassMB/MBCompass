package com.mubarak.mbcompass.core.sensors

import android.hardware.SensorManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SensorViewModelTest {

    @Test
    fun defaultState_isCorrect() = runTest {
        val vm = SensorViewModel()

        assertFalse(vm.trueNorthEnabled.value) // default is false
        assertNull(vm.location.value)

        assertEquals(
            "Sensor status unknown",
            vm.sensorStatusIcon.value.contentDescription
        )

        assertFalse(vm.accuracyAlertDialogState.value.show)
    }


    @Test
    fun highAccuracy_updatesIconCorrectly() = runTest {
        val vm = SensorViewModel()

        vm.updateSensorAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)

        val state = vm.sensorStatusIcon.value

        assertEquals("Sensor accuracy high", state.contentDescription)
        assertEquals(SensorManager.SENSOR_STATUS_ACCURACY_HIGH, state.accuracy)
    }

    @Test
    fun lowAccuracy_triggersDialogOnce() = runTest {
        val vm = SensorViewModel()

        vm.updateSensorAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_LOW)

        val dialog = vm.accuracyAlertDialogState.value
        assertTrue(dialog.show)

        // Call again shouldn't  trigger again
        vm.updateSensorAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_LOW)

        val dialogAgain = vm.accuracyAlertDialogState.value
        assertTrue(dialogAgain.show) // still true, not duplicated
    }

    @Test
    fun accuracyRecovery_resetsAutoDialogFlag() = runTest {
        val vm = SensorViewModel()

        // low, show dialog
        vm.updateSensorAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        assertTrue(vm.accuracyAlertDialogState.value.show)

        // Dismiss dialog
        vm.accuracyDialogDismissed()

        // High, resets flag
        vm.updateSensorAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)

        // Low again, should trigger again
        vm.updateSensorAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_LOW)

        assertTrue(vm.accuracyAlertDialogState.value.show)
    }

    @Test
    fun iconClick_showsDialogWithCurrentAccuracy() = runTest {
        val vm = SensorViewModel()

        vm.updateSensorAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM)

        vm.sensorStatusIconClicked() // shows dialogs

        val dialog = vm.accuracyAlertDialogState.value

        assertTrue(dialog.show)
        assertEquals(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM, dialog.accuracyForDialog)
    }

    @Test
    fun dialogDismiss_hidesDialog() = runTest {
        val vm = SensorViewModel()

        vm.updateSensorAccuracy(SensorManager.SENSOR_STATUS_UNRELIABLE)

        vm.accuracyDialogDismissed()

        assertFalse(vm.accuracyAlertDialogState.value.show)
    }

    @Test
    fun trueNorth_updatesCorrectly() = runTest {
        val vm = SensorViewModel()

        vm.setTrueNorthState(true)

        assertTrue(vm.trueNorthEnabled.value)
    }

}

