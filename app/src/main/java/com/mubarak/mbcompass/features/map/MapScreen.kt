// SPDX-License-Identifier: GPL-3.0-or-later
/*
 * Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
 * This project is licensed under GPL-3.0. Any derivative work must keep the same license,
 * retain this copyright notice, and provide proper attribution.
 */

package com.mubarak.mbcompass.features.map

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mubarak.mbcompass.MapProvider
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.core.location.LocationHelper
import com.mubarak.mbcompass.core.permission.PermissionHandler
import com.mubarak.mbcompass.data.AppPreferences
import com.mubarak.mbcompass.data.TrackRepository
import com.mubarak.mbcompass.data.preferences.UserPreferenceRepository
import com.mubarak.mbcompass.features.tracks.MapOverlayHelper
import com.mubarak.mbcompass.features.tracks.TrackerService
import com.mubarak.mbcompass.features.tracks.TrackingConstants
import com.mubarak.mbcompass.features.tracks.model.Track
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.mapsforge.MapsForgeTileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "MapScreen"

// osmdroid specific attributes saved as compose state
class MapOverlayState {
    var currentPositionOverlay: ItemizedIconOverlay<OverlayItem>? = null
    var currentTrackPolyline: Polyline? = null
    var currentTrackMarkersOverlay: ItemizedIconOverlay<OverlayItem>? = null
    var savedTrackPolyline: Polyline? = null
    var savedTrackMarkersOverlay: ItemizedIconOverlay<OverlayItem>? = null
}

@InstallIn(SingletonComponent::class)
@EntryPoint
interface MapScreenEntryPoint {
    fun trackRepository(): TrackRepository
    fun userPreferencesRepository(): UserPreferenceRepository
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    trackUri: String? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val trackRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            MapScreenEntryPoint::class.java
        ).trackRepository()
    }

    val userPreferencesRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext, MapScreenEntryPoint::class.java
        ).userPreferencesRepository()
    }

    val offlineMapPrefs by remember {
        userPreferencesRepository
            .getUserPreferenceStream
            .map { prefs -> prefs.isOfflineMapSource to prefs.offlineMapFolderPath }
    }.collectAsStateWithLifecycle(initialValue = false to "")

    val (useOfflineMaps, offlineMapFolder) = offlineMapPrefs


    val activity = LocalActivity.current as ComponentActivity
    val permissionHandler = remember {
        PermissionHandler(
            context = activity,
            shouldShowRationale = { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.location_permission_denied_message)
                )
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                context,
                R.string.notification_permission_denied_brief,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    var trackingState by remember { mutableIntStateOf(AppPreferences.loadTrackingState()) }
    var userInteraction by remember { mutableStateOf(false) }
    var currentBestLocation by remember {
        mutableStateOf(LocationHelper.getLastKnownLocation(context))
    }
    var currentTrack by remember { mutableStateOf(Track()) }

    var trackerService by remember { mutableStateOf<TrackerService?>(null) }
    var bound by remember { mutableStateOf(false) }

    var showEmptyTrackDialog by remember { mutableStateOf(false) }
    var showSaveTrackDialog by remember { mutableStateOf(false) }
    var pendingSaveService by remember { mutableStateOf<TrackerService?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val overlayState = remember { MapOverlayState() }
    val mapOverlayHelper = remember { MapOverlayHelper() }

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", 0)
        )

        MapsForgeTileSource.createInstance(activity.application)

        MapView(context).apply {
            overlays.add(CopyrightOverlay(context))
            val compassOverlay = CompassOverlay(
                context,
                InternalCompassOrientationProvider(context),
                this
            )
            compassOverlay.enableCompass()
            overlays.add(compassOverlay)

            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setTilesScaledToDpi(true)
            minZoomLevel = 3.coerceAtLeast(TileSourceFactory.MAPNIK.minimumZoomLevel).toDouble()
            maxZoomLevel = TileSourceFactory.MAPNIK.maximumZoomLevel.toDouble()

            setOnTouchListener { _, _ ->
                userInteraction = true
                false
            }
        }
    }

    LaunchedEffect(useOfflineMaps, offlineMapFolder) {
        MapProvider.applyMapSource(
            context = context,
            mapView = mapView,
            useOfflineMaps = useOfflineMaps,
            offlineMapFolder = offlineMapFolder,
        )
    }

    val uiHandler = remember { Handler(Looper.getMainLooper()) }

    val periodicRunnable = remember {
        object : Runnable {
            override fun run() {
                if (!bound) return
                trackerService?.let { service ->
                    currentBestLocation = service.currentBestLocation
                    currentTrack = service.currentTrack
                    trackingState = service.trackingState

                    markCurrentPosition(
                        mapView, mapOverlayHelper, overlayState,
                        currentBestLocation, trackingState, context
                    )

                    if (trackingState != TrackingConstants.STATE_TRACKING_NOT) {
                        overlayCurrentTrack(
                            mapView, mapOverlayHelper, overlayState,
                            currentTrack, trackingState, context
                        )
                    }

                    if (!userInteraction) {
                        centerMap(mapView, currentBestLocation)
                    }
                }
                uiHandler.postDelayed(this, TrackingConstants.UI_UPDATE_INTERVAL)
            }
        }
    }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as TrackerService.LocalBinder).getService()
                trackerService = service
                bound = true
                trackingState = service.trackingState
                uiHandler.removeCallbacks(periodicRunnable)
                uiHandler.postDelayed(periodicRunnable, 0)
                Log.d(TAG, "Service connected")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                bound = false
                trackerService = null
                uiHandler.removeCallbacks(periodicRunnable)
            }
        }
    }

    fun bindTrackerService() {
        context.bindService(
            Intent(context, TrackerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    @SuppressLint("LocalContextGetResourceValueCall")
    fun checkLocationServices() {
        if (!LocationHelper.isLocationEnabled(context)) {
            MapNotifications.showLocationOff(
                snackbarHostState = snackbarHostState,
                scope = scope,
                message = context.getString(R.string.location_off_message),
                actionLabel = context.getString(R.string.enable),
                onOpenSettings = { permissionHandler.openLocationSettings() }
            )
        }
    }

    fun onLocationPermissionGranted() {
        if (!LocationHelper.isLocationEnabled(context)) {
            checkLocationServices()
            return
        }
        if (trackUri == null) bindTrackerService()
        centerMap(mapView, currentBestLocation)
    }

    fun startTrackerServiceActual(resume: Boolean) {
        val intent = Intent(context, TrackerService::class.java).apply {
            action = if (resume) TrackerService.ACTION_RESUME else TrackerService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun startTracking(resume: Boolean) {
        if (!permissionHandler.hasLocationPermission()) {
            permissionHandler.requestLocationPermission(
                launcher = locationPermissionLauncher,
                onGranted = { startTrackerServiceActual(resume) },
                onDenied = {
                    Toast.makeText(context, R.string.location_permission_title,
                        Toast.LENGTH_SHORT).show()
                }
            )
            return
        }
        if (!LocationHelper.isLocationEnabled(context)) {
            checkLocationServices()
            return
        }
        if (!permissionHandler.hasNotificationPermission()) {
            permissionHandler.requestNotificationPermission(
                launcher = notificationPermissionLauncher,
                onGranted = { startTrackerServiceActual(resume) }
            )
            return
        }
        startTrackerServiceActual(resume)
    }

    fun saveTrack(service: TrackerService) {
        val savedTrack = service.currentTrack.copy(
            latitude = mapView.mapCenter.latitude,
            longitude = mapView.mapCenter.longitude,
            zoomLevel = mapView.zoomLevelDouble
        )
        scope.launch {
            withContext(Dispatchers.IO) {
                trackRepository.saveTrackAndUpdateTrack(savedTrack)
            }
            clearCurrentTrackOverlays(mapView, overlayState)
            service.clearTrack()
            Toast.makeText(context, R.string.track_saved, Toast.LENGTH_SHORT).show()
        }
    }

    fun handleSaveTrack() {
        trackerService?.let { service ->
            if (service.currentTrack.wayPoints.isEmpty()) {
                showEmptyTrackDialog = true
            } else {
                pendingSaveService = service
                showSaveTrackDialog = true
            }
        }
    }

    fun handleClearTrack() {
        if (currentTrack.wayPoints.isEmpty()) {
            trackerService?.clearTrack()
        } else {
            showDiscardDialog = true
        }
    }

    fun handleLocationButtonClick() {
        if (permissionHandler.hasLocationPermission()) {
            if (!LocationHelper.isLocationEnabled(context)) {
                checkLocationServices()
            } else {
                centerMap(mapView, currentBestLocation)
            }
        } else {
            permissionHandler.requestLocationPermission(
                launcher = locationPermissionLauncher,
                onGranted = { onLocationPermissionGranted() },
                onDenied = {
                    Toast.makeText(context, R.string.location_permission_required,
                        Toast.LENGTH_SHORT).show()
                }
            )
        }
    }


    remember(trackUri) {
        // reusing mapscreen for trackdetails screen also
        if (trackUri != null) {
            scope.launch {
                try {
                    val track = withContext(Dispatchers.IO) {
                        trackRepository.readTrackFromUri(trackUri.toUri())
                    }
                    if (track.wayPoints.isEmpty()) {
                        Toast.makeText(context, R.string.track_no_data, Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    updateSavedTrackOverlay(mapView, mapOverlayHelper, overlayState, track, context)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading track", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            mapView.controller.setZoom(AppPreferences.loadZoomLevel())
            mapView.controller.setCenter(
                GeoPoint(currentBestLocation.latitude, currentBestLocation.longitude)
            )
            markCurrentPosition(
                mapView, mapOverlayHelper, overlayState,
                currentBestLocation, trackingState, context
            )
        }
    }

    // lifecycle observer for osmdroid and service
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (trackUri == null && permissionHandler.hasLocationPermission()) {
                        bindTrackerService()
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    checkLocationServices()
                    if (bound) {
                        if (trackingState == TrackingConstants.STATE_TRACKING_ACTIVE) {
                            trackerService?.addGpsLocationListener()
                            trackerService?.addNetworkLocationListener()
                        }
                        uiHandler.removeCallbacks(periodicRunnable)
                        uiHandler.postDelayed(periodicRunnable, 0)
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    if (trackUri == null) {
                        val location = trackerService?.currentBestLocation ?: currentBestLocation
                        AppPreferences.saveZoomLevel(mapView.zoomLevelDouble)
                        AppPreferences.saveCurrentLocation(location)
                        userInteraction = false
                    }
                    if (bound && trackingState != TrackingConstants.STATE_TRACKING_ACTIVE) {
                        trackerService?.removeGpsLocationListener()
                        trackerService?.removeNetworkLocationListener()
                    }
                    uiHandler.removeCallbacks(periodicRunnable)
                }

                Lifecycle.Event.ON_STOP -> {
                    if (bound && trackUri == null) {
                        context.unbindService(serviceConnection)
                        bound = false
                    }
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            uiHandler.removeCallbacks(periodicRunnable)
            mapView.onDetach()
        }
    }

    val isViewOnlyMode = trackUri != null

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize(),
                update = {}
            )

            // only showing ui control on map screen
            if (!isViewOnlyMode) {
                MapControlsOverlay(
                    trackingState = trackingState,
                    snackbarHostState = snackbarHostState,
                    onLocationClick = { handleLocationButtonClick() },
                    onStartPauseResumeClick = {
                        when (trackingState) {
                            TrackingConstants.STATE_TRACKING_NOT -> startTracking(resume = false)
                            TrackingConstants.STATE_TRACKING_ACTIVE -> trackerService?.pauseTracking()
                            TrackingConstants.STATE_TRACKING_PAUSED -> startTracking(resume = true)
                        }
                    },
                    onSaveClick = { handleSaveTrack() },
                    onDiscardClick = { handleClearTrack() }
                )
            }
        }
    }


    if (showEmptyTrackDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrackDialog = false },
            title = { Text(stringResource(R.string.track_not_saved)) },
            text = { Text(stringResource(R.string.msg_empty_recording)) },
            confirmButton = {
                TextButton(onClick = {
                    showEmptyTrackDialog = false
                    trackerService?.resumeTracking()
                }) {
                    Text(stringResource(R.string.resume_recording))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrackDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // save dialog for recorded track
    if (showSaveTrackDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveTrackDialog = false
                pendingSaveService = null
            },
            title = { Text(stringResource(R.string.save_track)) },
            text = { Text(stringResource(R.string.save_track_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    showSaveTrackDialog = false
                    pendingSaveService?.let { saveTrack(it) }
                    pendingSaveService = null
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveTrackDialog = false
                    pendingSaveService = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // discard dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.discard_track)) },
            text = { Text(stringResource(R.string.discard_track_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    clearCurrentTrackOverlays(mapView, overlayState)
                    trackerService?.clearTrack()
                }) {
                    Text(
                        text = stringResource(R.string.discard),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun markCurrentPosition(
    mapView: MapView,
    helper: MapOverlayHelper,
    state: MapOverlayState,
    location: Location,
    trackingState: Int,
    context: Context
) {
    state.currentPositionOverlay?.let { mapView.overlays.remove(it) }
    state.currentPositionOverlay = helper.createMyLocationOverlay(context, location, trackingState)
    mapView.overlays.add(state.currentPositionOverlay)
    mapView.invalidate()
}

private fun overlayCurrentTrack(
    mapView: MapView,
    helper: MapOverlayHelper,
    state: MapOverlayState,
    track: Track,
    trackingState: Int,
    context: Context
) {
    state.currentTrackPolyline?.let { mapView.overlays.remove(it) }
    state.currentTrackMarkersOverlay?.let { mapView.overlays.remove(it) }

    if (track.wayPoints.isNotEmpty()) {
        state.currentTrackPolyline = helper.createTrackOverlay(context, track, trackingState)
        state.currentTrackMarkersOverlay = helper.createStartEndMarkersOverlay(
            context, track, displayMarkers = false
        )
        mapView.overlays.add(state.currentTrackPolyline)
        mapView.overlays.add(state.currentTrackMarkersOverlay)
        mapView.invalidate()
    }
}

private fun updateSavedTrackOverlay(
    mapView: MapView,
    helper: MapOverlayHelper,
    state: MapOverlayState,
    track: Track,
    context: Context
) {
    state.savedTrackPolyline?.let { mapView.overlays.remove(it) }
    state.savedTrackMarkersOverlay?.let { mapView.overlays.remove(it) }

    if (track.wayPoints.isNotEmpty()) {
        state.savedTrackPolyline = helper.createTrackOverlay(
            context, track, TrackingConstants.STATE_TRACKING_NOT
        )
        state.savedTrackMarkersOverlay = helper.createStartEndMarkersOverlay(
            context, track, displayMarkers = true
        )
        mapView.overlays.add(state.savedTrackPolyline)
        mapView.overlays.add(state.savedTrackMarkersOverlay)
    }
    centerOnTrack(mapView, track)
    mapView.invalidate()
}

private fun clearCurrentTrackOverlays(mapView: MapView, state: MapOverlayState) {
    state.currentTrackPolyline?.let { mapView.overlays.remove(it) }
    state.currentTrackMarkersOverlay?.let { mapView.overlays.remove(it) }
    state.currentTrackPolyline = null
    state.currentTrackMarkersOverlay = null
    mapView.invalidate()
}

private fun centerMap(mapView: MapView, location: Location) {
    mapView.controller.animateTo(GeoPoint(location.latitude, location.longitude))
}

private fun centerOnTrack(mapView: MapView, track: Track) {
    if (track.latitude != TrackingConstants.DEFAULT_LATITUDE &&
        track.longitude != TrackingConstants.DEFAULT_LONGITUDE
    ) {
        mapView.controller.setZoom(track.zoomLevel)
        mapView.controller.setCenter(GeoPoint(track.latitude, track.longitude))
    } else {
        if (track.wayPoints.isNotEmpty()) {
            val bounds = org.osmdroid.util.BoundingBox.fromGeoPoints(
                track.wayPoints.map { GeoPoint(it.latitude, it.longitude) }
            )
            mapView.post { mapView.zoomToBoundingBox(bounds, true, 100) }
        }
    }
}

// a singleton used here as a notification for location state via snackbar
object MapNotifications {

    fun showLocationOff(
        snackbarHostState: SnackbarHostState,
        scope: CoroutineScope,
        message: String,
        actionLabel: String,
        onOpenSettings: () -> Unit,
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onOpenSettings()
            }
        }
    }
}