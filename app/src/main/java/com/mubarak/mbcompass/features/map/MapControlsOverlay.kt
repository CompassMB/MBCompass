// SPDX-License-Identifier: GPL-3.0-or-later
/*
 * Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
 * This project is licensed under GPL-3.0. Any derivative work must keep the same license,
 * retain this copyright notice, and provide proper attribution.
 */

package com.mubarak.mbcompass.features.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.features.tracks.TrackingConstants

@Composable
fun MapControlsOverlay(
    trackingState: Int,
    snackbarHostState: SnackbarHostState,
    onLocationClick: () -> Unit,
    onStartPauseResumeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDiscardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPaused = trackingState == TrackingConstants.STATE_TRACKING_PAUSED

    Box(modifier = modifier.fillMaxSize()) {

        // used as for location service status state
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 60.dp)
        )

        FloatingActionButton(
            onClick = onLocationClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.my_location_24),
                contentDescription = stringResource(R.string.show_my_location)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPaused) {
                FloatingActionButton(
                    onClick = onDiscardClick,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete_24px),
                        contentDescription = stringResource(R.string.discard_track)
                    )
                }
            }

            // for start / pause / resume
            ExtendedFloatingActionButton(
                onClick = onStartPauseResumeClick,
                icon = {
                    Icon(
                        painter = painterResource(
                            when (trackingState) {
                                TrackingConstants.STATE_TRACKING_ACTIVE ->
                                    R.drawable.pause_circle_24px

                                else ->
                                    R.drawable.start_circle_24px
                            }
                        ),
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = stringResource(
                            when (trackingState) {
                                TrackingConstants.STATE_TRACKING_ACTIVE -> R.string.btn_pause
                                TrackingConstants.STATE_TRACKING_PAUSED -> R.string.btn_resume
                                else -> R.string.btn_start
                            }
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
            )

            if (isPaused) {
                FloatingActionButton(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(start = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.save_24dp),
                        contentDescription = stringResource(R.string.save)
                    )
                }
            }
        }
    }
}