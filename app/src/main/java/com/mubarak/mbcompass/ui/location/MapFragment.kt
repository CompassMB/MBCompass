package com.mubarak.mbcompass.ui.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.databinding.FragmentMapBinding
import org.maplibre.android.MapLibre
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.OnLocationCameraTransitionListener
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.permissions.PermissionsListener
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style


class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var lastLocation: Location? = null
    private lateinit var mapView: MapView
    private var permissionsManager: PermissionsManager? = null
    private var locationComponent: LocationComponent? = null
    private lateinit var maplibreMap: MapLibreMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MapLibre.getInstance(requireActivity().applicationContext)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val view = binding.root

        mapView = binding.locMapView
        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lastLocation =
                    savedInstanceState.getParcelable(SAVED_STATE_LOCATION, Location::class.java)
            }
        }
        mapView.onCreate(savedInstanceState)

        isPermissionGranted()
        return view
    }

    companion object {
        private const val SAVED_STATE_LOCATION = "saved_state_location"

        // TODO: this tile server may not suitable for production. Help needed
        private const val OPEN_FREE_MAP_LIBERTY = "https://tiles.openfreemap.org/styles/liberty"
    }

    private fun isPermissionGranted() {
        val context = requireContext()
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            mapView.getMapAsync(this)
        } else {
            permissionsManager = PermissionsManager(object : PermissionsListener {
                override fun onExplanationNeeded(permissionsToExplain: List<String>) {
                    Toast.makeText(
                        context,
                        R.string.permission_rationale,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionResult(granted: Boolean) {
                    if (granted) {
                        mapView.getMapAsync(this@MapFragment)
                    } else {
                        requireActivity().finish()
                    }
                }
            })
            permissionsManager!!.requestLocationPermissions(requireActivity())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    @SuppressLint("MissingPermission")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
        if (locationComponent != null) {
            outState.putParcelable(SAVED_STATE_LOCATION, locationComponent!!.lastKnownLocation)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(mapLibreMap: MapLibreMap) {
        val context = requireContext()
        this.maplibreMap = mapLibreMap
        maplibreMap.setStyle(OPEN_FREE_MAP_LIBERTY) { style: Style ->
            mapLibreMap.uiSettings.isLogoEnabled = false
            mapLibreMap.uiSettings.setAttributionMargins(0, 0, 0, 10)
            mapLibreMap.uiSettings.setAttributionTintColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.attr_color
                )
            )

            mapLibreMap.setMinZoomPreference(1.7)
            mapLibreMap.setMaxZoomPreference(15.0) //  0.0, to = 25.5

            locationComponent = maplibreMap.locationComponent
            val locationComponentOptions =
                LocationComponentOptions.builder(context)
                    .pulseEnabled(true)
                    .pulseColor(ContextCompat.getColor(context, R.color.brand_color))
                    .foregroundTintColor(
                        ContextCompat.getColor(
                            context,
                            R.color.brand_color
                        )
                    )  // Set color of user location
                    .build()
            val locationComponentActivationOptions =
                buildLocationComponentActivationOptions(style, locationComponentOptions)
            locationComponent!!.activateLocationComponent(locationComponentActivationOptions)
            locationComponent!!.isLocationComponentEnabled = true

            locationComponent!!.setCameraMode(CameraMode.TRACKING, object :
                OnLocationCameraTransitionListener {
                override fun onLocationCameraTransitionFinished(cameraMode: Int) {
                    locationComponent!!.zoomWhileTracking(4.0)
                }

                override fun onLocationCameraTransitionCanceled(cameraMode: Int) {}
            })

            locationComponent!!.forceLocationUpdate(lastLocation)
        }
    }

    private fun buildLocationComponentActivationOptions(
        style: Style,
        locationComponentOptions: LocationComponentOptions
    ): LocationComponentActivationOptions {
        return LocationComponentActivationOptions
            .builder(requireContext(), style)
            .locationComponentOptions(locationComponentOptions)
            .useDefaultLocationEngine(true)
            .locationEngineRequest(
                LocationEngineRequest.Builder(750)
                    .setFastestInterval(750)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .build()
            )
            .build()
    }
}