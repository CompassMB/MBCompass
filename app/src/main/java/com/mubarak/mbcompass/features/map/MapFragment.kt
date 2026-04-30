// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.features.map

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarHostState
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.core.location.LocationHelper
import com.mubarak.mbcompass.core.permission.PermissionHandler
import com.mubarak.mbcompass.ui.FragmentNotifications
import com.mubarak.mbcompass.data.AppPreferences
import com.mubarak.mbcompass.data.TrackRepository
import com.mubarak.mbcompass.databinding.FragmentMapBinding
import com.mubarak.mbcompass.features.tracks.MapOverlayHelper
import com.mubarak.mbcompass.features.tracks.TrackerService
import com.mubarak.mbcompass.features.tracks.TrackingConstants
import com.mubarak.mbcompass.features.tracks.model.Track
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var trackerService: TrackerService? = null
    private var bound = false

    @Inject
    lateinit var trackRepository: TrackRepository
    private lateinit var snackbarHostState: SnackbarHostState
    private lateinit var mapView: MapView
    private lateinit var controller: IMapController
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var btnStart: LinearLayout
    private lateinit var btnStartIcon: ImageView
    private lateinit var btnStartTxt: TextView
    private lateinit var btnSave: ImageButton
    private lateinit var btnDiscard: ImageButton
    private lateinit var locationButton: ImageButton

    private val uiHandler = Handler(Looper.getMainLooper())
    private var trackingState = TrackingConstants.STATE_TRACKING_NOT
    private var userInteraction = false
    private lateinit var currentBestLocation: Location
    private var currentTrack: Track = Track()
    private var trackUriToDisplay: String? = null

    private var currentPositionOverlay: ItemizedIconOverlay<OverlayItem>? = null
    private var currentTrackPolyline: Polyline? = null
    private var currentTrackMarkersOverlay: ItemizedIconOverlay<OverlayItem>? = null
    private var savedTrackPolyline: Polyline? = null
    private var savedTrackMarkersOverlay: ItemizedIconOverlay<OverlayItem>? = null

    companion object {
        const val TAG = "MapFragment"
        const val ARG_TRACK_URI = "track_uri"

        fun newInstance(trackUri: String? = null): MapFragment {
            return MapFragment().apply {
                arguments = Bundle().apply {
                    trackUri?.let { putString(ARG_TRACK_URI, it) }
                }
            }
        }
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onLocationPermissionGranted()
            } else {
                FragmentNotifications.showSnackbar(
                    this,
                    snackbarHostState,
                    getString(R.string.location_permission_denied_message),
                    )
            }
        }


    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                FragmentNotifications.showToast(
                    this,
                    getString(R.string.notification_permission_denied_brief),
                    )
            }
        }

    private val activityRecognitionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                FragmentNotifications.showSnackbar(
                    this,
                    snackbarHostState,
                    getString(R.string.activity_recognition_denied_brief)
                )
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentBestLocation = LocationHelper.getLastKnownLocation(requireContext())
        trackingState = AppPreferences.loadTrackingState()
        permissionHandler = PermissionHandler(this)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        Configuration.getInstance().userAgentValue = requireContext().packageName
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", 0)
        )

        mapView = binding.mapView
        controller = mapView.controller

        btnStart = binding.btnStartTrack
        btnStartIcon = binding.btnStartTrackIcon
        btnStartTxt = binding.btnStartTrackText
        btnSave = binding.btnSaveTrack
        btnDiscard = binding.btnDiscardTrack
        locationButton = binding.btnLocation

        setupMap()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snackbarHostState = FragmentNotifications.attachSnackbarHost(
            composeView = binding.snackbarHost,
            bottomOffsetDp = 60 // bottom pdding
        )

        trackUriToDisplay = arguments?.getString(ARG_TRACK_URI)

        trackUriToDisplay?.let { trackUri ->
            Log.d(TAG, "Loading saved track from URI: $trackUri")
            loadAndDisplayTrack(trackUri)
        } ?: run {
            restoreMapState()
        }

        setupButtons()

        checkLocationServices()
    }

    override fun onStart() {
        super.onStart()

        if (trackUriToDisplay == null && permissionHandler.hasLocationPermission()) {
            bindTrackerService()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        checkLocationServices()

        if (bound) {
            if (trackingState != TrackingConstants.STATE_TRACKING_ACTIVE) {
                trackerService?.addGpsLocationListener()
                trackerService?.addNetworkLocationListener()
            }

            uiHandler.removeCallbacks(periodicLocationRequestRunnable)
            uiHandler.postDelayed(periodicLocationRequestRunnable, 0)
        }

        updateMainButton(trackingState)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        saveMapState()

        if (bound && trackingState != TrackingConstants.STATE_TRACKING_ACTIVE) {
            trackerService?.removeGpsLocationListener()
            trackerService?.removeNetworkLocationListener()
        }

        uiHandler.removeCallbacks(periodicLocationRequestRunnable)
    }

    override fun onStop() {
        super.onStop()

        if (bound && trackUriToDisplay == null) {
            requireContext().unbindService(serviceConnection)
            bound = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uiHandler.removeCallbacks(periodicLocationRequestRunnable)
        _binding = null
        mapView.onDetach()
    }


    private fun checkLocationServices() {
        if (!LocationHelper.isLocationEnabled(requireContext())) {
            // Location services OFF
            FragmentNotifications.showLocationOff(this,snackbarHostState) {
                // open location settings
                permissionHandler.openLocationSettings()

            }
        }
    }


    private fun onLocationPermissionGranted() {
        Log.d(TAG, "Location permission granted - binding service")

        // Check location services first
        if (!LocationHelper.isLocationEnabled(requireContext())) {
            checkLocationServices()
            return
        }

        // Bind service
        if (trackUriToDisplay == null) {
            bindTrackerService()
        }

        // Center on current location
        centerMap(currentBestLocation, animated = true)
    }


    private fun bindTrackerService() {
        requireContext().bindService(
            Intent(requireContext(), TrackerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun setupButtons() {
        locationButton.setOnClickListener {
            handleLocationButtonClick()
        }

        // don't show start track button on TrackFragment
        val isViewOnlyMode = trackUriToDisplay != null
        btnStart.isVisible = !isViewOnlyMode
        locationButton.isVisible = !isViewOnlyMode

        btnStart.setOnClickListener {
            handleStartButton()
        }

        btnSave.setOnClickListener {
            handleSaveTrack()
        }

        btnDiscard.setOnClickListener {
            handleClearTrack()
        }
    }


    private fun handleLocationButtonClick() {
        when {
            // Has permission, center map
            permissionHandler.hasLocationPermission() -> {
                // Check location services
                if (!LocationHelper.isLocationEnabled(requireContext())) {
                    checkLocationServices()
                } else {
                    centerMap(currentBestLocation, animated = true)
                }
            }

            // No permission, request with education
            else -> {
                permissionHandler.requestLocationPermission(
                    launcher = locationPermissionLauncher,
                    onGranted = { onLocationPermissionGranted() },
                    onDenied = {
                        Toast.makeText(
                            requireContext(),
                            R.string.location_permission_required,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }


    private fun handleStartButton() {
        when (trackingState) {
            TrackingConstants.STATE_TRACKING_NOT -> startTracking(resume = false)
            TrackingConstants.STATE_TRACKING_ACTIVE -> trackerService?.pauseTracking()
            TrackingConstants.STATE_TRACKING_PAUSED -> startTracking(resume = true)
        }
    }


    private fun startTracking(resume: Boolean) {
        // Location permission
        if (!permissionHandler.hasLocationPermission()) {
            permissionHandler.requestLocationPermission(
                launcher = locationPermissionLauncher,
                onGranted = { startTracking(resume) },
                onDenied = {
                    Toast.makeText(
                        requireContext(),
                        R.string.location_permission_title,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            return
        }

        //  Location services
        if (!LocationHelper.isLocationEnabled(requireContext())) {
            checkLocationServices()
            return
        }

        //  Notification permission (Android 13+)
        if (!permissionHandler.hasNotificationPermission()) {
            permissionHandler.requestNotificationPermission(
                launcher = notificationPermissionLauncher,
                onGranted = { startTracking(resume) },
                onDenied = { startTracking(resume) } // Continue anyway
            )
            return
        }

        // Activity recognition (Android 10+)
        if (!permissionHandler.hasActivityRecognitionPermission()) {
            permissionHandler.requestActivityRecognitionPermission(
                launcher = activityRecognitionLauncher,
                onGranted = { startTrackerServiceActual(resume) },
                onDenied = { startTrackerServiceActual(resume) } // Continue anyway
            )
            return
        }

        // all permissions, start service
        startTrackerServiceActual(resume)
    }


    private fun startTrackerServiceActual(resume: Boolean) {
        val intent = Intent(requireContext(), TrackerService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }

        if (resume) {
            trackerService?.resumeTracking()
        } else {
            trackerService?.startTracking()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackerService.LocalBinder
            trackerService = binder.getService()
            bound = true

            trackingState = trackerService?.trackingState ?: TrackingConstants.STATE_TRACKING_NOT
            updateMainButton(trackingState)

            uiHandler.removeCallbacks(periodicLocationRequestRunnable)
            uiHandler.postDelayed(periodicLocationRequestRunnable, 0)

            Log.d(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            trackerService = null
            uiHandler.removeCallbacks(periodicLocationRequestRunnable)
        }
    }

    private fun updateMainButton(trackingState: Int) {
        this.trackingState = trackingState

        when (trackingState) {
            TrackingConstants.STATE_TRACKING_NOT -> {
                btnStartIcon.setImageResource(R.drawable.start_circle_24px)
                btnStartTxt.text = getString(R.string.btn_start)
                btnSave.isGone = true
                btnDiscard.isGone = true
            }

            TrackingConstants.STATE_TRACKING_ACTIVE -> {
                btnStartIcon.setImageResource(R.drawable.pause_circle_24px)
                btnStartTxt.text = getString(R.string.btn_pause)
                btnSave.isGone = true
                btnDiscard.isGone = true
            }

            TrackingConstants.STATE_TRACKING_PAUSED -> {
                btnStartIcon.setImageResource(R.drawable.start_circle_24px)
                btnStartTxt.text = getString(R.string.btn_resume)
                btnSave.isVisible = true
                btnDiscard.isVisible = true
            }
        }
    }

    private fun handleSaveTrack() {
        trackerService?.let { service ->
            if (service.currentTrack.wayPoints.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.track_not_saved)
                    .setMessage(R.string.msg_empty_recording)
                    .setPositiveButton(R.string.resume_recording) { _, _ ->
                        trackerService?.resumeTracking()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.save_track)
                    .setMessage(R.string.save_track_confirmation)
                    .setPositiveButton(R.string.save) { _, _ ->
                        saveTrack(service)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }

    private fun saveTrack(service: TrackerService) {
        val savedTrack = service.currentTrack.copy(
            latitude = mapView.mapCenter.latitude,
            longitude = mapView.mapCenter.longitude,
            zoomLevel = mapView.zoomLevelDouble
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                trackRepository.saveTrackAndUpdateTrack(savedTrack)
            }

            clearCurrentTrackOverlays()
            service.clearTrack()
            updateMainButton(trackingState)

            Toast.makeText(requireContext(), R.string.track_saved, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleClearTrack() {
        if (currentTrack.wayPoints.isEmpty()) {
            trackerService?.clearTrack()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.discard_track)
                .setMessage(R.string.discard_track_confirmation)
                .setPositiveButton(R.string.discard) { _, _ ->
                    clearCurrentTrackOverlays()
                    trackerService?.clearTrack()
                    updateMainButton(trackingState)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private val periodicLocationRequestRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!bound) return

            trackerService?.let { service ->
                val previousTrackingState = trackingState

                currentBestLocation = service.currentBestLocation
                currentTrack = service.currentTrack
                trackingState = service.trackingState

                if (trackingState != previousTrackingState) {
                    updateMainButton(trackingState)
                }

                markCurrentPosition(currentBestLocation, trackingState)

                if (trackingState != TrackingConstants.STATE_TRACKING_NOT) {
                    overlayCurrentTrack(currentTrack, trackingState)
                }

                if (!userInteraction) {
                    centerMap(currentBestLocation, animated = true)
                }
            }

            uiHandler.postDelayed(this, TrackingConstants.UI_UPDATE_INTERVAL)
        }
    }

    private fun setupMap() {
        mapView.overlays.add(CopyrightOverlay(context))

        val compassOverlay = CompassOverlay(
            context,
            InternalCompassOrientationProvider(context),
            mapView
        )
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        mapView.setOnTouchListener { _, _ ->
            userInteraction = true
            false
        }

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapView.setTilesScaledToDpi(true)
        mapView.minZoomLevel = 3.coerceAtLeast(TileSourceFactory.MAPNIK.minimumZoomLevel).toDouble()
        mapView.maxZoomLevel = TileSourceFactory.MAPNIK.maximumZoomLevel.toDouble()
    }

    private fun restoreMapState() {
        val zoomLevel = AppPreferences.loadZoomLevel()
        controller.setZoom(zoomLevel)
        controller.setCenter(GeoPoint(currentBestLocation.latitude, currentBestLocation.longitude))
        markCurrentPosition(currentBestLocation, trackingState)
    }

    private fun saveMapState() {
        if (trackUriToDisplay == null) {
            val location = trackerService?.currentBestLocation ?: currentBestLocation
            AppPreferences.saveZoomLevel(mapView.zoomLevelDouble)
            AppPreferences.saveCurrentLocation(location)
            userInteraction = false
        }
    }

    private fun markCurrentPosition(location: Location, trackingState: Int) {
        currentPositionOverlay?.let { mapView.overlays.remove(it) }

        val mapOverlayHelper = MapOverlayHelper()
        currentPositionOverlay = mapOverlayHelper.createMyLocationOverlay(
            requireContext(),
            location,
            trackingState
        )
        mapView.overlays.add(currentPositionOverlay)
        mapView.invalidate()
    }

    private fun overlayCurrentTrack(track: Track, trackingState: Int) {
        currentTrackPolyline?.let { mapView.overlays.remove(it) }
        currentTrackMarkersOverlay?.let { mapView.overlays.remove(it) }

        if (track.wayPoints.isNotEmpty()) {
            val mapOverlayHelper = MapOverlayHelper()

            currentTrackPolyline = mapOverlayHelper.createTrackOverlay(
                requireContext(),
                track,
                trackingState
            )

            currentTrackMarkersOverlay = mapOverlayHelper.createStartEndMarkersOverlay(
                requireContext(),
                track,
                displayMarkers = false
            )

            mapView.overlays.add(currentTrackPolyline)
            mapView.overlays.add(currentTrackMarkersOverlay)
            mapView.invalidate()
        }
    }

    private fun updateSavedTrackOverlay(track: Track, centerMap: Boolean) {
        savedTrackPolyline?.let { mapView.overlays.remove(it) }
        savedTrackMarkersOverlay?.let { mapView.overlays.remove(it) }

        if (track.wayPoints.isNotEmpty()) {
            val mapOverlayHelper = MapOverlayHelper()

            savedTrackPolyline = mapOverlayHelper.createTrackOverlay(
                requireContext(),
                track,
                TrackingConstants.STATE_TRACKING_NOT
            )

            savedTrackMarkersOverlay = mapOverlayHelper.createStartEndMarkersOverlay(
                requireContext(),
                track,
                displayMarkers = true
            )

            mapView.overlays.add(savedTrackPolyline)
            mapView.overlays.add(savedTrackMarkersOverlay)
        }

        if (centerMap) {
            centerOnTrack(track)
        }

        mapView.invalidate()
    }

    private fun clearCurrentTrackOverlays() {
        currentTrackPolyline?.let { mapView.overlays.remove(it) }
        currentTrackMarkersOverlay?.let { mapView.overlays.remove(it) }
        currentTrackPolyline = null
        currentTrackMarkersOverlay = null
        mapView.invalidate()
    }

    private fun centerMap(location: Location, animated: Boolean = false) {
        val position = GeoPoint(location.latitude, location.longitude)
        when (animated) {
            true -> controller.animateTo(position)
            false -> controller.setCenter(position)
        }
        userInteraction = false
    }

    private fun centerOnTrack(track: Track) {
        if (track.latitude != TrackingConstants.DEFAULT_LATITUDE &&
            track.longitude != TrackingConstants.DEFAULT_LONGITUDE
        ) {
            controller.setZoom(track.zoomLevel)
            controller.setCenter(GeoPoint(track.latitude, track.longitude))
        } else {
            val bounds = getTrackBounds(track)
            if (bounds != null) {
                mapView.post {
                    mapView.zoomToBoundingBox(bounds, true, 100)
                }
            }
        }
    }

    private fun getTrackBounds(track: Track): BoundingBox? {
        if (track.wayPoints.isEmpty()) return null
        val points = track.wayPoints.map { GeoPoint(it.latitude, it.longitude) }
        return BoundingBox.fromGeoPoints(points)
    }

    private fun loadAndDisplayTrack(trackUri: String) {
        lifecycleScope.launch {
            try {
                val track = withContext(Dispatchers.IO) {
                    trackRepository.readTrackFromUri(trackUri.toUri())
                }

                if (track.wayPoints.isEmpty()) {
                    Toast.makeText(requireContext(), "Track has no data", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                updateSavedTrackOverlay(track, centerMap = true)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading track", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}