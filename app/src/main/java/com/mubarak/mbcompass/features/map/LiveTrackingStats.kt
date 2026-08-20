// SPDX-License-Identifier: GPL-3.0-or-later
/*
 * Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
 * This project is licensed under GPL-3.0. Any derivative work must keep the same license,
 * retain this copyright notice, and provide proper attribution.
 */

package com.mubarak.mbcompass.features.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.features.tracks.TrackingConstants
import com.mubarak.mbcompass.utils.DateTimeFormatter
import com.mubarak.mbcompass.utils.LengthUnitHelper


@Composable
fun LiveTrackingStats(
    liveDistance: Float,
    liveDuration: Long,
    trackingState: Int,
    modifier: Modifier = Modifier
) {
    val isVisible = trackingState != TrackingConstants.STATE_TRACKING_NOT

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.statusBarsPadding()
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 4.dp,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatChip(
                    icon = R.drawable.ic_distance_24px,
                    value = LengthUnitHelper.convertDistanceToString(liveDistance),
                    contentDescription = stringResource(R.string.distance)
                )

                VerticalDivider(
                    modifier = Modifier.size(width = 1.dp, height = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                StatChip(
                    icon = R.drawable.ic_duration24px,
                    value = DateTimeFormatter.formatDurationTime(liveDuration),
                    contentDescription = stringResource(R.string.duration)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: Int,
    value: String,
    contentDescription: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}