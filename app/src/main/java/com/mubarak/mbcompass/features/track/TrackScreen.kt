// SPDX-License-Identifier: GPL-3.0-or-later
/*
 * Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
 * This project is licensed under GPL-3.0. Any derivative work must keep the same license,
 * retain this copyright notice, and provide proper attribution.
 */

package com.mubarak.mbcompass.features.track

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.data.TrackRepository
import com.mubarak.mbcompass.features.tracks.model.Track
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import androidx.core.net.toUri
import com.mubarak.mbcompass.features.map.MapScreen

private const val TAG = "TrackScreen"
private const val MIME_TYPE_GPX = "application/gpx+xml"


@EntryPoint
@InstallIn(SingletonComponent::class)
interface TrackScreenEntryPoint {
    fun trackRepository(): TrackRepository
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    trackUri: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val trackRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            TrackScreenEntryPoint::class.java
        ).trackRepository()
    }

    var track by remember { mutableStateOf<Track?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // load track
    LaunchedEffect(trackUri) {
        try {
            val loaded = withContext(Dispatchers.IO) {
                trackRepository.readTrackFromUri(Uri.parse(trackUri))
            }
            track = loaded
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error loading track", e)
            loadError = e.message
        }
    }

    LaunchedEffect(loadError) {
        loadError?.let {
            Toast.makeText(context, "Error loading track: $it", Toast.LENGTH_LONG).show()
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // GPX save launcher
    val saveGpxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val targetUri = result.data?.data ?: return@rememberLauncherForActivityResult
        val sourceUri = Uri.parse(track?.gpxUriString) ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val input = context.contentResolver.openInputStream(sourceUri)
                    val output = context.contentResolver.openOutputStream(targetUri)
                    if (input != null && output != null) {
                        input.copyTo(output)
                        input.close()
                        output.close()
                    } else {
                        throw Exception("Failed to open streams for file copy")
                    }
                }
                Toast.makeText(context, R.string.toast_gpx_saved, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving GPX", e)
                Toast.makeText(context, R.string.toast_error_saving_gpx, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun openSaveGpxDialog() {
        val t = track ?: return
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MIME_TYPE_GPX
            putExtra(Intent.EXTRA_TITLE, trackRepository.getGpxFileName(t))
        }
        try {
            saveGpxLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening save dialog", e)
            Toast.makeText(context, R.string.install_file_manager, Toast.LENGTH_LONG).show()
        }
    }

    fun shareGpxViaShareSheet() {
        val t = track ?: return
        try {
            val gpxFile = t.gpxUriString.toUri().toFile()
            val gpxShareUri = FileProvider.getUriForFile(
                context,
                "${context.applicationContext.packageName}.provider",
                gpxFile
            )
            val shareIntent = Intent.createChooser(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    data = gpxShareUri
                    type = MIME_TYPE_GPX
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    putExtra(Intent.EXTRA_STREAM, gpxShareUri)
                },
                null
            )
            val packageManager: PackageManager = context.packageManager
            if (shareIntent.resolveActivity(packageManager) != null) {
                context.startActivity(shareIntent)
            } else {
                Toast.makeText(
                    context, R.string.install_file_manager,
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing GPX", e)
            Toast.makeText(context, R.string.toast_error_saving_gpx, Toast.LENGTH_LONG).show()
        }
    }

    fun deleteTrack() {
        val t = track ?: return
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    trackRepository.deleteTrack(t.getTrackId())
                }
                Toast.makeText(context, R.string.track_deleted, Toast.LENGTH_SHORT).show()
                onNavigateBack()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting track", e)
                Toast.makeText(
                    context, R.string.toast_error_deleting_track,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.track_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_24px),
                            contentDescription = stringResource(R.string.nav_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // reusing the mapscreen just to display the track
            MapScreen(
                modifier = Modifier.fillMaxSize(),
                trackUri = trackUri
            )

            FloatingActionButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 16.dp,
                        end = 16.dp
                    ),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Icon(
                    painter = painterResource(R.drawable.info_24px),
                    contentDescription = stringResource(R.string.track_details)
                )
            }

            if (showBottomSheet) {
                track?.let { loadedTrack ->
                    TrackStatsBottomSheet(
                        track = loadedTrack,
                        sheetState = sheetState,
                        onDismissRequest = { showBottomSheet = false },
                        onShareClick = {
                            showBottomSheet = false
                            showShareDialog = true
                        },
                        onDeleteClick = {
                            showBottomSheet = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showShareDialog) {
        ShareOptionsDialog(
            onSaveToFile = {
                showShareDialog = false
                openSaveGpxDialog()
            },
            onShareViaApps = {
                showShareDialog = false
                shareGpxViaShareSheet()
            },
            onDismiss = { showShareDialog = false }
        )
    }

    if (showDeleteDialog) {
        track?.let { loadedTrack ->
            DeleteTrackDialog(
                trackName = loadedTrack.name,
                onConfirm = {
                    showDeleteDialog = false
                    deleteTrack()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
private fun ShareOptionsDialog(
    onSaveToFile: () -> Unit,
    onShareViaApps: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.share_track)) },
        text = {
            Column {
                TextButton(
                    onClick = onSaveToFile
                ) {
                    Text(
                        text = stringResource(R.string.save_gpx_to_file),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                TextButton(
                    onClick = onShareViaApps,
                ) {
                    Text(
                        text = stringResource(R.string.share_gpx_via_apps),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DeleteTrackDialog(
    trackName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.delete_24px),
                contentDescription = null
            )
        },
        title = { Text(stringResource(R.string.delete_track_title)) },
        text = {
            Text(
                text = stringResource(R.string.delete_track_confirmation, trackName)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}