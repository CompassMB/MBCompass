package com.mubarak.mbcompass.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Build
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class AndroidSensorEventListenerTest {

    private lateinit var context: Context
    private lateinit var sensorManager: SensorManager
    private lateinit var windowManager: WindowManager
    private lateinit var azimuthValueListener: AndroidSensorEventListener.AzimuthValueListener
    private lateinit var sensorEventListener: AndroidSensorEventListener

    @Before
    fun setUp() {
        context = mockk<Context>(relaxed = true)
        sensorManager = mockk(relaxed = true)
        windowManager = mockk(relaxed = true)
        every { context.getSystemService(Context.SENSOR_SERVICE) } returns sensorManager
        every { context.getSystemService(Context.WINDOW_SERVICE) } returns windowManager
        azimuthValueListener = mockk(relaxed = true)
        sensorEventListener = AndroidSensorEventListener(context)
        sensorEventListener.setAzimuthListener(azimuthValueListener)
    }

    @Test
    fun `onSensorChanged should update accelerometer and magnetometer readings and call updateOrientationAngles`() {
        val accelerometerValues = floatArrayOf(1f, 2f, 3f)
        val magnetometerValues = floatArrayOf(4f, 5f, 6f)

        val sensorAccel = mockk<Sensor>(relaxed = true) {
            every { type } returns Sensor.TYPE_ACCELEROMETER
        }
        val accelerometerEvent = mockk<SensorEvent>(relaxed = true)
        accelerometerEvent.sensor = sensorAccel

        val accelValuesField = accelerometerEvent.javaClass.getDeclaredField("values")
        accelValuesField.isAccessible = true
        accelValuesField.set(accelerometerEvent, accelerometerValues)

        val sensorMagnet = mockk<Sensor>(relaxed = true) {
            every { type } returns Sensor.TYPE_MAGNETIC_FIELD
        }
        val magnetometerEvent = mockk<SensorEvent>(relaxed = true)
        magnetometerEvent.sensor = sensorMagnet

        val magnetValuesField = magnetometerEvent.javaClass.getDeclaredField("values")
        magnetValuesField.isAccessible = true
        magnetValuesField.set(magnetometerEvent, magnetometerValues)

        sensorEventListener.onSensorChanged(accelerometerEvent)
        sensorEventListener.onSensorChanged(magnetometerEvent)

        assertArrayEquals(accelerometerValues, sensorEventListener.accelerometerReading, 0.001f)
        assertArrayEquals(magnetometerValues, sensorEventListener.magnetometerReading, 0.001f)

        verify { sensorEventListener.updateOrientationAngles() }
    }

    @Test
    fun `onAccuracyChanged should show toast when accuracy is unreliable`() {
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<Int>(), any()) } returns toast
        sensorEventListener.onAccuracyChanged(mockk(), SensorManager.SENSOR_STATUS_UNRELIABLE)
        verify { Toast.makeText(any(), any<Int>(), any()) }
    }

    @Test
    fun `onAccuracyChanged should show toast when accuracy is low`() {
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<Int>(), any()) } returns toast
        sensorEventListener.onAccuracyChanged(mockk(), SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        verify { Toast.makeText(any(), any<Int>(), any()) }
    }

    @Test
    fun `updateOrientationAngles should call listener with correct values`() {
        mockkStatic(SensorManager::class)
        every { SensorManager.getRotationMatrix(any(), any(), any(), any()) } returns true
        every { SensorManager.getOrientation(any(), any()) } returns floatArrayOf(1.0f, 2.0f, 3.0f)
        val display = mockk<Display>()
        every {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay
        } returns display
        every { display.rotation } returns Surface.ROTATION_0
        every { SensorManager.remapCoordinateSystem(any(),any(),any(),any()) } returns true

        sensorEventListener.updateOrientationAngles()

        verify { azimuthValueListener.onAzimuthValueChange(any()) }
        verify { azimuthValueListener.onMagneticStrengthChange(any()) }
    }

    @Test
    fun `registerSensor should register listeners`() {
        val accelerometer = mockk<Sensor>()
        val magnetometer = mockk<Sensor>()
        every { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) } returns accelerometer
        every { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) } returns magnetometer

        sensorEventListener.registerSensor()

        verify { sensorManager.registerListener(any(), accelerometer, any(), any<Int>()) }
        verify { sensorManager.registerListener(any(), magnetometer, any(), any<Int>()) }
    }

    @Test
    fun `unregisterSensorListener should unregister listeners`() {
        sensorEventListener.unregisterSensorListener()
        verify { sensorManager.unregisterListener(sensorEventListener) }
    }

    @Test
    fun `setAzimuthListener should set the listener`() {
        val newListener = mockk<AndroidSensorEventListener.AzimuthValueListener>()
        sensorEventListener.setAzimuthListener(newListener)
        assertEquals(newListener, sensorEventListener.azimuthValueListener)
    }
}