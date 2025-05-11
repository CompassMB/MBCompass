// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
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

        Configuration.getInstance()
            .load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))

        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK) // Uses OSM tile server tile policy applies
        mapView.setMultiTouchControls(true)
        mapView.setTilesScaledToDpi(true)

        val mapController = mapView.controller

        mapController.setZoom(20.1)
        mapController.setCenter(GeoPoint(48.8583, 2.2944)) // Default location

        val mCopyrightOverlay = CopyrightOverlay(context)
        mapView.overlays.add(mCopyrightOverlay)

        val mCompassOverlay = CompassOverlay(
            context, InternalCompassOrientationProvider(context),
            mapView
        )
        mCompassOverlay.enableCompass()
        mapView.overlays.add(mCompassOverlay)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            enableLocationOverlay()
        }
    }

    private fun enableLocationOverlay() {
        myLocationOverlay =
            MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView).apply {
                enableMyLocation()
                enableFollowLocation()
            }
        mapView.overlays.add(myLocationOverlay)
        mapView.invalidate()
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
    }
}
