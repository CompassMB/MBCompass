// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.utils.ToDegree
import kotlin.math.sqrt

private const val TAG = "SensorListener"

class AndroidSensorEventListener(
    private val context: Context
) : SensorEventListener {

    private val rotationMatrix = FloatArray(9)
    private val adjustedRotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val magnetometerReading = FloatArray(3)

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    interface AzimuthValueListener {
        fun onAzimuthValueChange(degree: Float)
        fun onMagneticStrengthChange(strengthInUt: Float)
    }

    private var azimuthValueListener: AzimuthValueListener? = null

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display.rotation
            } else {
                val windowManager =
                    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.rotation
            }

            if (rotation != null) {
                when (rotation) {
                    Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Y,
                        adjustedRotationMatrix
                    )

                    Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_Y,
                        SensorManager.AXIS_MINUS_X,
                        adjustedRotationMatrix
                    )

                    Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_MINUS_X,
                        SensorManager.AXIS_MINUS_Y,
                        adjustedRotationMatrix
                    )

                    Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_MINUS_Y,
                        SensorManager.AXIS_X,
                        adjustedRotationMatrix
                    )
                }

                SensorManager.getOrientation(adjustedRotationMatrix, orientationAngles)
                val azimuth = orientationAngles[0]
                val toDegree = ToDegree.toDegree(azimuth)
                azimuthValueListener?.onAzimuthValueChange(toDegree)
            }
        }else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        findMagneticStrength()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        when (sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                    accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW
                ) {
                    Toast.makeText(context, R.string.calibration_required, Toast.LENGTH_LONG).show()
                }
            }

            Sensor.TYPE_ROTATION_VECTOR -> Log.d(TAG, "Rotational Vector Sensor @$accuracy")
        }

    }

    private fun findMagneticStrength() {
        val magneticStrength =
            sqrt(((magnetometerReading[0] * magnetometerReading[0]) + (magnetometerReading[1] * magnetometerReading[1]) + (magnetometerReading[2] * magnetometerReading[2])))

        azimuthValueListener?.onMagneticStrengthChange(magneticStrength)
    }

    fun registerSensor() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.also { rotationVector ->
            registerRotationVectorSensor(sensorManager, rotationVector)
        } ?: run {
            Log.d(TAG, "ROTATION_VECTOR not available")
            Toast.makeText(context, R.string.rotation_sensor_not_available, Toast.LENGTH_LONG).show()
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticFieldSensor ->
            registerMagneticFieldSensor(sensorManager, magneticFieldSensor)
        } ?: run {
            Log.d(TAG, "Magnetometer not available")
            Toast.makeText(context, R.string.magnetometer_not_available, Toast.LENGTH_LONG).show()
            // Display alert dialog to user
        }

    }

    private fun registerRotationVectorSensor(
        sensorManager: SensorManager,
        rotationVectorSensor: Sensor
    ) {
        sensorManager.registerListener(
            this,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    private fun registerMagneticFieldSensor(
        sensorManager: SensorManager,
        magneticFieldSensor: Sensor
    ) {
        sensorManager.registerListener(
            this,
            magneticFieldSensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun unregisterSensorListener() {
        sensorManager.unregisterListener(this)
    }

    fun setAzimuthListener(listener: AzimuthValueListener) {
        azimuthValueListener = listener
    }
}