// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.features.track

import com.mubarak.mbcompass.R
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mubarak.mbcompass.data.TrackRepository
import com.mubarak.mbcompass.databinding.FragmentTrackViewBinding
import com.mubarak.mbcompass.features.map.MapFragment
import com.mubarak.mbcompass.features.tracks.model.Track
import com.mubarak.mbcompass.utils.DateTimeFormatter
import com.mubarak.mbcompass.utils.LengthUnitHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class TrackViewFragment : Fragment() {

    companion object {
        private const val TAG = "TrackViewFragment"
        const val ARG_TRACK_URI = "track_uri"
        private const val MIME_TYPE_GPX = "application/gpx+xml"

        fun newInstance(trackUri: String): TrackViewFragment {
            return TrackViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TRACK_URI, trackUri)
                }
            }
        }
    }

    @Inject
    lateinit var trackRepository: TrackRepository

    private var _binding: FragmentTrackViewBinding? = null
    private val binding get() = _binding!!

    private var track: Track? = null
    private var trackUri: String? = null

    // activity result launcher for saving GPX
    private val saveGpxLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        this::handleSaveGpxResult
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackUri = arguments?.getString(ARG_TRACK_URI)
        if (trackUri == null) {
            Log.e(TAG, "No track URI provided")
            Toast.makeText(requireContext(), "Error: No track specified", Toast.LENGTH_SHORT).show()
            return
        }

        loadTrack(trackUri!!)
        setupMapFragment(trackUri!!)
        setupActionButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadTrack(trackUri: String) {
        lifecycleScope.launch {
            try {
                val loadedTrack = withContext(Dispatchers.IO) {
                    trackRepository.readTrackFromUri(trackUri.toUri())
                }

                track = loadedTrack
                displayTrackStatistics(loadedTrack)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading track", e)
                Toast.makeText(
                    requireContext(),
                    "Error loading track",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupMapFragment(trackUri: String) {
        val fragmentManager = childFragmentManager
        val existingFragment = fragmentManager.findFragmentById(R.id.map_container) as? MapFragment

        if (existingFragment == null) {
            fragmentManager.beginTransaction()
                .replace(R.id.map_container, MapFragment.newInstance(trackUri))
                .commitNow()

            Log.d(TAG, "MapFragment added for track: $trackUri")
        }
    }

    private fun displayTrackStatistics(track: Track) {
        with(binding) {
            // Track name and date
            trackName.text = track.name
            trackDate.text = DateTimeFormatter.formatDateTimeString(track.recordingStart)

            trackDuration.text = DateTimeFormatter.formatDurationTime(track.duration)
            trackDistance.text = LengthUnitHelper.convertDistanceToString(track.length)

            trackWaypoints.text = track.wayPoints.size.toString()

            // Average speed calculation
            trackAvgSpeed.text = if (track.duration > 0) {
                val speedKmh = (track.length / 1000.0) / (track.duration / 3600000.0)
                String.format("%.1f km/h", speedKmh)
            } else {
                "0.0 km/h"
            }

            if (track.positiveElevation != 0.0 || track.negativeElevation != 0.0) {
                elevationCard.isVisible = true
                trackUphill.text = String.format("+%.0f m", track.positiveElevation)
                trackDownhill.text = String.format("%.0f m", kotlin.math.abs(track.negativeElevation))
            } else {
                elevationCard.isVisible = false
            }

            if (track.maxAltitude > 0.0) {
                altitudeRangeCard.isVisible = true
                trackMinAltitude.text = String.format("%.0f m", track.minAltitude)
                trackMaxAltitude.text = String.format("%.0f m", track.maxAltitude)
            } else {
                altitudeRangeCard.isVisible = false
            }

            // Recording details
            if (track.recordingStop != 0L) {
                recordingInfoCard.isVisible = true
                trackRecordingStart.text = "Started: ${DateTimeFormatter.formatDateTimeString(track.recordingStart)}"
                trackRecordingStop.text = "Ended: ${DateTimeFormatter.formatDateTimeString(track.recordingStop)}"
            } else {
                recordingInfoCard.isVisible = false
            }
        }

        Log.d(TAG, "Displayed stats for track: ${track.name} (${track.wayPoints.size} waypoints)")
    }

    private fun setupActionButtons() {
        binding.fabShare.setOnClickListener {
            showShareOptions()
        }
    }


    private fun showShareOptions() {
        val options = arrayOf("Save GPX to file", "Share GPX via apps", "Delete track")

        AlertDialog.Builder(requireContext())
            .setTitle("Track Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openSaveGpxDialog()
                    1 -> shareGpxViaShareSheet()
                    2 -> showDeleteConfirmation()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openSaveGpxDialog() {
        track?.let { track ->
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = MIME_TYPE_GPX
                putExtra(Intent.EXTRA_TITLE, trackRepository.getGpxFileName(track))
            }

            try {
                saveGpxLauncher.launch(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to save GPX.", e)
                Toast.makeText(requireContext(), "Please install a file manager app", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleSaveGpxResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val sourceUri: Uri = Uri.parse(track?.gpxUriString)
            val targetUri: Uri? = result.data?.data

            if (targetUri != null) {
                lifecycleScope.launch {
                    try {
                        copyFile(sourceUri, targetUri)
                        Toast.makeText(requireContext(), "GPX file saved successfully", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving GPX", e)
                        Toast.makeText(requireContext(), "Error saving GPX file", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private suspend fun copyFile(sourceUri: Uri, targetUri: Uri) {
        withContext(Dispatchers.IO) {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(sourceUri)
            val outputStream = requireContext().contentResolver.openOutputStream(targetUri)

            if (inputStream != null && outputStream != null) {
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            } else {
                throw Exception("Failed to open streams for file copy")
            }
        }
    }

    private fun shareGpxViaShareSheet() {
        track?.let { track ->
            try {
                val gpxFile = Uri.parse(track.gpxUriString).toFile()
                val gpxShareUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireActivity().applicationContext.packageName}.provider",
                    gpxFile
                )

                val shareIntent: Intent = Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        data = gpxShareUri
                        type = MIME_TYPE_GPX
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        putExtra(Intent.EXTRA_STREAM, gpxShareUri)
                    },
                    null
                )

                val packageManager: PackageManager? = requireActivity().packageManager
                if (packageManager != null && shareIntent.resolveActivity(packageManager) != null) {
                    startActivity(shareIntent)
                } else {
                    Toast.makeText(requireContext(), "Please install a file manager app", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error sharing GPX", e)
                Toast.makeText(requireContext(), "Error sharing GPX file", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDeleteConfirmation() {
        track?.let { track ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Track")
                .setMessage("Are you sure you want to delete this track?\n\n- ${track.name}")
                .setIcon(R.drawable.delete_24px)
                .setPositiveButton("Delete") { _, _ ->
                    deleteTrack()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun deleteTrack() {
        track?.let { track ->
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        trackRepository.deleteTrack(track.getTrackId())
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting track", e)
                    Toast.makeText(requireContext(), "Error deleting track", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}