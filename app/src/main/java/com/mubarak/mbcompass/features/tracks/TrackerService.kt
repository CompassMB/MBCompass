// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.features.tracks

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mubarak.mbcompass.MainActivity
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.core.location.LocationHelper
import com.mubarak.mbcompass.data.AppPreferences
import com.mubarak.mbcompass.data.TrackRepository
import com.mubarak.mbcompass.features.tracks.model.Track
import com.mubarak.mbcompass.utils.DateTimeFormatter
import com.mubarak.mbcompass.utils.LengthUnitHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date
import java.util.GregorianCalendar
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "TrackerService"
        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START = "action_start"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_RESUME = "action_resume"
        const val ACTION_STOP = "action_stop"
    }

    @Inject
    lateinit var trackRepository: TrackRepository

    private val binder = LocalBinder()
    private val uiHandler: Handler = Handler(Looper.getMainLooper())
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var trackingState: Int = TrackingConstants.STATE_TRACKING_NOT
        set(value) {
            field = value
            AppPreferences.saveTrackingState(value)
            Log.d(TAG, "Tracking state changed: $value")
        }

    var currentBestLocation: Location = LocationHelper.getDefaultLocation()

    var currentTrack = Track()

    var isGpsProviderActive: Boolean = false
    var isNetworkProviderActive: Boolean = false
    var useGpsOnly: Boolean = false
    var locationAccuracyMultiplier: Int = 1

    var stepCountOffset: Float = 0f
    var isResumedAfterPause: Boolean = false
    var lastTempSaveTime: Date = GregorianCalendar.getInstance().time

    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager
    private lateinit var notificationManager: NotificationManager

    private var gpsLocationListener: LocationListener? = null
    private var networkLocationListener: LocationListener? = null
    private var isGpsListenerRegistered = false
    private var isNetworkListenerRegistered = false

    private val altitudeSmoothingQueue = AltitudeSmoother(13,6)

    inner class LocalBinder : Binder() {
        fun getService(): TrackerService = this@TrackerService
    }

    override fun onCreate() {
        super.onCreate()

        locationAccuracyMultiplier = AppPreferences.loadAccuracyMultiplier()

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()

        isGpsProviderActive = LocationHelper.isGpsEnabled(locationManager)
        isNetworkProviderActive = LocationHelper.isNetworkEnabled(locationManager)

        gpsLocationListener = createLocationListener()
        networkLocationListener = createLocationListener()

        trackingState = AppPreferences.loadTrackingState()
        currentBestLocation = LocationHelper.getLastKnownLocation(this)

        // Load temp track if service was restarted
        currentTrack = trackRepository.readTrackFromUri(trackRepository.getTempFileUri())

        Log.d(TAG, "Service onCreate - state: $trackingState, " +
                "location: ${currentBestLocation.latitude}, ${currentBestLocation.longitude}")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            // Service restarted by system
            when (trackingState) {
                TrackingConstants.STATE_TRACKING_ACTIVE -> {
                    Log.w(TAG, "Service killed by OS. Restoring active tracking.")
                    currentTrack = trackRepository.readTrackFromUri(trackRepository.getTempFileUri())
                    resumeTracking()
                }
                TrackingConstants.STATE_TRACKING_PAUSED -> {
                    Log.w(TAG, "Service restarted in paused state.")
                    currentTrack = trackRepository.readTrackFromUri(trackRepository.getTempFileUri())
                }
            }
            return START_STICKY
        }

        when (intent.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        addGpsLocationListener()
        addNetworkLocationListener()
        return binder
    }

    override fun onRebind(intent: Intent?) {
        addGpsLocationListener()
        addNetworkLocationListener()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (trackingState != TrackingConstants.STATE_TRACKING_ACTIVE) {
            removeGpsLocationListener()
            removeNetworkLocationListener()
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy called")

        if (trackingState == TrackingConstants.STATE_TRACKING_ACTIVE) {
            Log.w(TAG, "Service destroyed while tracking - pausing to save data")
            pauseTracking(calledFromDestroy = true)
        }

        stopForegroundCompat(removeNotification = true)
        notificationManager.cancel(NOTIFICATION_ID)

        removeGpsLocationListener()
        removeNetworkLocationListener()
        ioScope.cancel()
    }

    fun startTracking(newTrack: Boolean = true) {
        addGpsLocationListener()
        addNetworkLocationListener()

        if (newTrack) {
            currentTrack = Track(
                name = DateTimeFormatter.formatDateTimeString(System.currentTimeMillis()),
                recordingStart = System.currentTimeMillis()
            )
            stepCountOffset = 0f
        }

        trackingState = TrackingConstants.STATE_TRACKING_ACTIVE

        startStepCounter()
        uiHandler.removeCallbacks(periodicTrackUpdateRunnable)
        uiHandler.postDelayed(periodicTrackUpdateRunnable, 0)

        startForeground(NOTIFICATION_ID, createNotification())

        Log.d(TAG, "Started tracking")
    }

    fun pauseTracking(calledFromDestroy: Boolean = false) {
        currentTrack.recordingStop = System.currentTimeMillis()
        trackingState = TrackingConstants.STATE_TRACKING_PAUSED

        uiHandler.removeCallbacks(periodicTrackUpdateRunnable)
        sensorManager.unregisterListener(this)
        altitudeSmoothingQueue.reset()

        if (calledFromDestroy) {
            // block until saved
            runBlocking(Dispatchers.IO) {
                trackRepository.saveTempTrack(currentTrack)
            }
        } else {
            ioScope.launch {
                trackRepository.saveTempTrack(currentTrack)
            }
        }

        updateNotification()
        stopForegroundCompat(removeNotification = false)

        Log.d(TAG, "Paused tracking")
    }

    fun resumeTracking() {
        if (currentTrack.wayPoints.isEmpty()) {
            currentTrack = trackRepository.readTrackFromUri(trackRepository.getTempFileUri())
        }

        if (currentTrack.wayPoints.isNotEmpty()) {
            val lastIndex = currentTrack.wayPoints.size - 1
            currentTrack.wayPoints[lastIndex].isStopOver = true
        }

        currentTrack.durationOfPause += GpxBuilder.calculateDurationOfPause(currentTrack.recordingStop)
        isResumedAfterPause = true

        trackingState = TrackingConstants.STATE_TRACKING_ACTIVE

        addGpsLocationListener()
        addNetworkLocationListener()

        startStepCounter()
        uiHandler.removeCallbacks(periodicTrackUpdateRunnable)
        uiHandler.postDelayed(periodicTrackUpdateRunnable, 0)

        startForeground(NOTIFICATION_ID, createNotification())

        Log.d(TAG, "Resumed tracking")
    }

    fun stopTracking() {
        trackingState = TrackingConstants.STATE_TRACKING_NOT

        uiHandler.removeCallbacks(periodicTrackUpdateRunnable)
        sensorManager.unregisterListener(this)
        removeGpsLocationListener()
        removeNetworkLocationListener()

        currentTrack = Track()
        trackRepository.deleteTempFile()

        stopForegroundCompat(removeNotification = true)
        stopSelf()

        Log.d(TAG, "Stopped tracking")
    }

    fun clearTrack() {
        uiHandler.removeCallbacks(periodicTrackUpdateRunnable)
        currentTrack = Track()
        trackRepository.deleteTempFile()

        trackingState = TrackingConstants.STATE_TRACKING_NOT

        stopForegroundCompat(removeNotification = true)
        notificationManager.cancel(NOTIFICATION_ID)

        Log.d(TAG, "Cleared track")
    }


    @Suppress("DEPRECATION")
    private fun stopForegroundCompat(removeNotification: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val flags = if (removeNotification) {
                STOP_FOREGROUND_REMOVE
            } else {
                STOP_FOREGROUND_DETACH
            }
            stopForeground(flags)
        } else {
            stopForeground(removeNotification)
        }
    }


    private val periodicTrackUpdateRunnable: Runnable = object : Runnable {
        override fun run() {
            val result: Pair<Boolean, Track> = GpxBuilder.addWayPointToTrack(
                track = currentTrack,
                location = currentBestLocation,
                accuracyMultiplier = locationAccuracyMultiplier,
                resumed = isResumedAfterPause
            )

            val waypointAdded: Boolean = result.first
            currentTrack = result.second

            if (waypointAdded) {
                if (isResumedAfterPause) isResumedAfterPause = false

                // Altitude smoothing
                val previousAltitude: Double = altitudeSmoothingQueue.getAverage()
                val currentAltitude = currentBestLocation.altitude

                if (currentAltitude != TrackingConstants.DEFAULT_ALTITUDE) {
                    altitudeSmoothingQueue.add(currentAltitude)
                }

                if (altitudeSmoothingQueue.isPrepared) {
                    val smoothedAltitude = altitudeSmoothingQueue.getAverage()
                    currentTrack = LocationHelper.calculateElevationDifferences(
                        smoothedAltitude,
                        previousAltitude,
                        currentTrack
                    )
                }

                // Auto-save temp track
                val now = GregorianCalendar.getInstance().time
                if (now.time - lastTempSaveTime.time > TrackingConstants.SAVE_TEMP_TRACK_INTERVAL) {
                    lastTempSaveTime = now
                    ioScope.launch {
                        trackRepository.saveTempTrack(currentTrack)
                    }
                }
            }

            updateNotification()
            uiHandler.postDelayed(this, TrackingConstants.WAYPOINT_INTERVAL)
        }
    }

    // TODO: move this continuous location listener into separate class
    private fun createLocationListener(): LocationListener {
        return object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (LocationHelper.isBestLocation(location, currentBestLocation)) {
                    currentBestLocation = location
                    Log.v(TAG, "Location updated: ${location.latitude}, ${location.longitude}, " +
                            "accuracy: ${location.accuracy}m, provider: ${location.provider}")
                }
            }

            override fun onProviderEnabled(provider: String) {
                when (provider) {
                    LocationManager.GPS_PROVIDER -> {
                        isGpsProviderActive = LocationHelper.isGpsEnabled(locationManager)
                    }
                    LocationManager.NETWORK_PROVIDER -> {
                        isNetworkProviderActive = LocationHelper.isNetworkEnabled(locationManager)
                    }
                }
            }

            override fun onProviderDisabled(provider: String) {
                when (provider) {
                    LocationManager.GPS_PROVIDER -> {
                        isGpsProviderActive = LocationHelper.isGpsEnabled(locationManager)
                    }
                    LocationManager.NETWORK_PROVIDER -> {
                        isNetworkProviderActive = LocationHelper.isNetworkEnabled(locationManager)
                    }
                }
            }

            @Deprecated("Deprecated in API 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
    }

    fun addGpsLocationListener() {
        if (isGpsListenerRegistered) return

        isGpsProviderActive = LocationHelper.isGpsEnabled(locationManager)
        if (!isGpsProviderActive) return

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        gpsLocationListener?.let {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, it)
            isGpsListenerRegistered = true
            Log.v(TAG, "Added GPS location listener")
        }
    }

    fun addNetworkLocationListener() {
        if (isNetworkListenerRegistered) return

        isNetworkProviderActive = LocationHelper.isNetworkEnabled(locationManager)
        if (!isNetworkProviderActive || useGpsOnly) return

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        networkLocationListener?.let {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, it)
            isNetworkListenerRegistered = true
            Log.v(TAG, "Added Network location listener")
        }
    }

    fun removeGpsLocationListener() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            gpsLocationListener?.let { locationManager.removeUpdates(it) }
            isGpsListenerRegistered = false
            Log.v(TAG, "Removed GPS location listener")
        }
    }

    fun removeNetworkLocationListener() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            networkLocationListener?.let { locationManager.removeUpdates(it) }
            isNetworkListenerRegistered = false
            Log.v(TAG, "Removed Network location listener")
        }
    }

    // TODO: move this logic into separate class
    private fun startStepCounter() {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            currentTrack.stepCount = -1f
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            if (stepCountOffset == 0f) {
                stepCountOffset = (event.values[0] - 1) - currentTrack.stepCount
            }
            currentTrack.stepCount = event.values[0] - stepCountOffset
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_tracking),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_tracking_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // TODO: move it to a separate class
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 10, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.tracks))
            .setContentText(getNotificationText())
            .setSmallIcon(R.drawable.ic_nav_24px)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        when (trackingState) {
            TrackingConstants.STATE_TRACKING_ACTIVE -> {
                val pauseIntent = Intent(this, TrackerService::class.java).apply {
                    action = ACTION_PAUSE
                }
                val pausePendingIntent = PendingIntent.getService(
                    this, ACTION_PAUSE.hashCode(), pauseIntent, PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    R.drawable.pause_circle_24px,
                    getString(R.string.btn_pause),
                    pausePendingIntent
                )
            }
            TrackingConstants.STATE_TRACKING_PAUSED -> {
                val resumeIntent = Intent(this, TrackerService::class.java).apply {
                    action = ACTION_RESUME
                }
                val resumePendingIntent = PendingIntent.getService(
                    this, ACTION_RESUME.hashCode(), resumeIntent, PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    R.drawable.start_circle_24px,
                    getString(R.string.btn_resume),
                    resumePendingIntent
                )
            }
        }

        return builder.build()
    }


    private fun updateNotification() {
        val notificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true  // Assuming enabled on API 23
        }

        if (notificationsEnabled) {
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
    }

    private fun getNotificationText(): String {
        val distance = LengthUnitHelper.convertDistanceToString(currentTrack.length)
        val duration = DateTimeFormatter.formatDurationTime(currentTrack.duration)
        return "$distance • $duration"
    }

    // altitude smoother for GPS, raw values are too noisy
    class AltitudeSmoother(
        private val capacity: Int,
        private val minSamples: Int
    ) {

        private val values = ArrayDeque<Double>(capacity)
        private var sum: Double = 0.0

        val size: Int
            get() = values.size

        val isPrepared: Boolean
            get() = size >= minSamples

        fun add(value: Double) {
            if (values.size == capacity) {
                sum -= values.removeFirst()
            }

            values.addLast(value)
            sum += value
        }

        fun getAverage(): Double {
            return if (values.isEmpty()) 0.0 else sum / values.size
        }

        fun reset() {
            values.clear()
            sum = 0.0
        }
    }
}