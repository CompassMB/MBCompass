// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.features.track

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.data.TrackRepository
import com.mubarak.mbcompass.databinding.FragmentTrackViewBinding
import com.mubarak.mbcompass.features.map.MapFragment
import com.mubarak.mbcompass.features.tracks.model.Track
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

    private var fab: ImageButton? = null

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

        setupMapFragment(trackUri!!)
        loadTrackAndSetupBottomSheet(trackUri!!)
        setupFAB()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fab = null
        _binding = null
    }

// reuse existing map fragment
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
    private fun loadTrackAndSetupBottomSheet(trackUri: String) {
        lifecycleScope.launch {
            try {
                val loadedTrack = withContext(Dispatchers.IO) {
                    trackRepository.readTrackFromUri(trackUri.toUri())
                }

                track = loadedTrack
                setupBottomSheet(loadedTrack)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading track", e)
                Toast.makeText(
                    requireContext(),
                    "Error loading track: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setupBottomSheet(track: Track) {
        val composeView = binding.bottomSheetCompose as ComposeView

        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                var showBottomSheet by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = false
                )

                MaterialTheme {
                    if (showBottomSheet) {
                        TrackStatsBottomSheet(
                            track = track,
                            sheetState = sheetState,
                            onDismissRequest = {
                                showBottomSheet = false
                                fab?.visibility = VISIBLE
                            },
                            onShareClick = {
                                showBottomSheet = false
                                showShareOptions()
                            },
                            onDeleteClick = {
                                showBottomSheet = false
                                showDeleteConfirmation()
                            }
                        )
                    }

                    // update FAB visibility based on sheet state
                    LaunchedEffect(showBottomSheet) {
                        if (showBottomSheet) {
                            fab?.visibility = GONE
                        } else {
                            fab?.visibility = VISIBLE
                        }
                    }
                }

                DisposableEffect(Unit) {
                    fab?.setOnClickListener {
                        showBottomSheet = true
                    }
                    onDispose { }
                }
            }
        }
    }


    private fun setupFAB() {
        fab = ImageButton(requireContext()).apply {
            setImageResource(R.drawable.info_24px)
            setBackgroundResource(R.drawable.fab_backgnd)
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 48, 48)
            }
        }

        (binding.root as ViewGroup).addView(fab)

        // align it top end
        val layoutParams = fab!!.layoutParams as FrameLayout.LayoutParams
        layoutParams.gravity = android.view.Gravity.TOP or android.view.Gravity.END
        layoutParams.setMargins(0, 16,
            resources.getDimensionPixelSize(R.dimen.fab_margin),
            resources.getDimensionPixelSize(R.dimen.fab_margin)
        )
        fab!!.layoutParams = layoutParams
    }

    private fun showShareOptions() {
        val options = arrayOf("Save GPX to file", "Share GPX via apps")

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.share_track)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openSaveGpxDialog()
                    1 -> shareGpxViaShareSheet()
                }
            }
            .setNegativeButton(R.string.cancel, null)
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
                Log.e(TAG, getString(R.string.toast_error_saving_gpx), e)
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
                        Toast.makeText(requireContext(), R.string.toast_gpx_saved, Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving GPX", e)
                        Toast.makeText(requireContext(), R.string.toast_error_saving_gpx, Toast.LENGTH_LONG).show()
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
                Toast.makeText(requireContext(), R.string.toast_error_saving_gpx, Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun showDeleteConfirmation() {
        track?.let { track ->
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_track_title)
                .setMessage("Are you sure you want to delete this track?\n\n- ${track.name}")
                .setIcon(R.drawable.delete_24px)
                .setPositiveButton(R.string.delete) { _, _ ->
                    deleteTrack()
                }
                .setNegativeButton(R.string.cancel, null)
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

                    Toast.makeText(requireContext(), R.string.track_deleted, Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()

                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting track", e)
                    Toast.makeText(requireContext(), R.string.toast_error_deleting_track, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}