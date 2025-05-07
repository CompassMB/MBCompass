// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.location

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.compose.ui.util.fastCbrt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MapViewModel by viewModels()

    private var locationManager: LocationManager? = null
    private lateinit var locationListener: LocationListener

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val webView = binding.mapWV

        // loading initial world map before we fetch the location
        binding.mapWV.loadUrl("file:///android_asset/map.html?lat=0.0&lon=0.0&zoom=0&pin=false")

        Toast.makeText(requireContext(), R.string.location_progress,Toast.LENGTH_LONG).show()

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(AndroidWebInterface(
            onLatLngReceived = { lat, lon ->
                viewModel.latitude = lat
                viewModel.longitude = lon
            },
            onZoomReceived = { zoom ->
                viewModel.zoom = zoom
            }
        ), "NativeInterface")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationManager = ActivityCompat.getSystemService(requireContext(), LocationManager::class.java)

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            startLocationUpdates()
        }

        loadMapFromState()
    }

    private fun loadMapFromState() {
        val lat = viewModel.latitude
        val lon = viewModel.longitude
        val zoom = viewModel.zoom
        if (lat != null && lon != null) {
            binding.mapWV.loadUrl("file:///android_asset/map.html?lat=$lat&lon=$lon&zoom=$zoom&pin=true")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        locationManager?.removeUpdates(locationListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val zoom = viewModel.zoom

                    viewModel.latitude = lat
                    viewModel.longitude = lon

                    binding.mapWV.loadUrl("file:///android_asset/map.html?lat=$lat&lon=$lon&zoom=$zoom&pin=true")
                    Log.d("MapFragment", "Lat: $lat, Lon: $lon")
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000L,
                5f,
                locationListener
            )
        }
    }
}


private class AndroidWebInterface(
    val onLatLngReceived: (Double, Double) -> Unit,
    val onZoomReceived: (Int) -> Unit
) {
    @JavascriptInterface
    fun transferLatLon(lat: Double, lon: Double) {
        onLatLngReceived(lat, lon)
    }

    @JavascriptInterface
    fun transferZoom(level: Int) {
        onZoomReceived(level)
    }
}
