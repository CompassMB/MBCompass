// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.databinding.FragmentMapBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private var myLocationOverlay: MyLocationNewOverlay? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission(), { isGranted ->
            if (isGranted) {
                enableLocationOverlay()
            } else {
                Toast.makeText(requireContext(), R.string.permission_rationale, Toast.LENGTH_SHORT)
                    .show()
            }
        })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        Configuration.getInstance().userAgentValue =
            requireContext().applicationContext?.packageName

        Configuration.getInstance()
            .load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))

        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK) // Uses OSM tile server tile policy applies
        mapView.setMultiTouchControls(true)
        mapView.setTilesScaledToDpi(true)

        mapView.minZoomLevel = 3.coerceAtLeast(TileSourceFactory.MAPNIK.minimumZoomLevel).toDouble()
        mapView.maxZoomLevel = TileSourceFactory.MAPNIK.maximumZoomLevel.toDouble()

        val mapController = mapView.controller

        mapController.setZoom(20.1)
        mapController.setCenter(GeoPoint(48.8583, 2.2944)) // Default location (Eiffel tower)

        val mCopyrightOverlay = CopyrightOverlay(context)
        mapView.overlays.add(mCopyrightOverlay)

        val mCompassOverlay = CompassOverlay(
            context, InternalCompassOrientationProvider(context),
            mapView
        )
        mCompassOverlay.enableCompass()
        mapView.overlays.add(mCompassOverlay)

        binding.btnLocation.setOnClickListener {
            checkAndEnableLocation()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAndEnableLocation()
    }

    private fun enableLocationOverlay() {
        // Only add the overlay if it null already exist or does not exist in the mapView
        if (myLocationOverlay == null || !mapView.overlays.contains(myLocationOverlay)) {
            // Remove any potentially old overlay first
            myLocationOverlay?.let { mapView.overlays.remove(it) }

            myLocationOverlay =
                MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView).apply {
                    val currentArrow = BitmapFactory.decodeResource(
                        context?.resources, R.drawable.my_arrow_nav
                    )

                    setPersonAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                    setDirectionIcon(currentArrow)
                    setDirectionAnchor(.5f, .5f)

                    enableMyLocation()
                    enableFollowLocation()
                }
            mapView.overlays.add(myLocationOverlay)
        } else {
            // If it already exists, just ensure it's enabled
            myLocationOverlay?.enableMyLocation()
            myLocationOverlay?.enableFollowLocation() // center the map on the user's location
        }
        mapView.invalidate()
    }

    private fun checkAndEnableLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            enableLocationOverlay()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        myLocationOverlay?.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        myLocationOverlay?.disableMyLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mapView.onDetach()
    }
}
