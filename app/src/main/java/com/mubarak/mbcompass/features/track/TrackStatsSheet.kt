// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.features.track

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.features.tracks.model.Track
import com.mubarak.mbcompass.utils.DateTimeFormatter
import com.mubarak.mbcompass.utils.LengthUnitHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackStatsBottomSheet(
    track: Track,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )
        }
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                bottom = 40.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Column {
                    Text(
                        text = track.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = DateTimeFormatter.formatDateTimeString(track.recordingStart),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                StatsGrid(track = track)
            }

            item {
                ElevationCard(track = track)
            }

            if (track.maxAltitude > 0.0) {
                item {
                    AltitudeRangeCard(
                        minAltitude = track.minAltitude,
                        maxAltitude = track.maxAltitude
                    )
                }
            }

            if (track.recordingStop != 0L) {
                item {
                    RecordingInfoCard(
                        startTime = track.recordingStart,
                        endTime = track.recordingStop
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                ActionButtons(
                    onShareClick = onShareClick,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }

}

@Composable
private fun StatsGrid(track: Track) {
    val avgSpeed = if (track.duration > 0)
        (track.length / 1000.0) / (track.duration / 3_600_000.0)
    else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = R.drawable.ic_duration24px,
                label = "Duration",
                value = DateTimeFormatter.formatDurationTime(track.duration),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = R.drawable.ic_distance_24px,
                label = "Distance",
                value = LengthUnitHelper.convertDistanceToString(track.length),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = R.drawable.location_icon24px,
                label = "Points",
                value = track.wayPoints.size.toString(),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = R.drawable.ic_speed_24px,
                label = "Avg Speed",
                value = "%.1f km/h".format(avgSpeed),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatTile(
    icon: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ElevationCard(track: Track) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Elevation",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            ElevationChart(
                waypoints = track.wayPoints,
                useMetric = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            if (track.positiveElevation != 0.0 || track.negativeElevation != 0.0) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ElevationStat(
                        icon = R.drawable.arrow_back_24px,
                        iconRotation = 90f,
                        label = "Ascent",
                        value = "+%.0f m".format(track.positiveElevation),
                        modifier = Modifier.weight(1f)
                    )
                    ElevationStat(
                        icon = R.drawable.arrow_back_24px,
                        iconRotation = 270f,
                        label = "Descent",
                        value = "−%.0f m".format(kotlin.math.abs(track.negativeElevation)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ElevationStat(
    icon: Int,
    iconRotation: Float,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { rotationZ = iconRotation },
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AltitudeRangeCard(minAltitude: Double, maxAltitude: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AltitudeTile(
                icon = R.drawable.altitude_low_24px,
                label = "Lowest",
                value = "%.0f m".format(minAltitude),
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(52.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            AltitudeTile(
                icon = R.drawable.altitude_high_24px,
                label = "Highest",
                value = "%.0f m".format(maxAltitude),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AltitudeTile(
    icon: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecordingInfoCard(startTime: Long, endTime: Long) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(
                text = "Recording",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            RecordingRow(label = "Started", value = DateTimeFormatter.formatDateTimeString(startTime))
            Spacer(Modifier.height(6.dp))
            RecordingRow(label = "Ended", value = DateTimeFormatter.formatDateTimeString(endTime))
        }
    }
}

@Composable
private fun RecordingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionButtons(
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onShareClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.share_24px),
                contentDescription = "Share",
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Share", style = MaterialTheme.typography.labelLarge)
        }

        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.delete_24px),
                contentDescription = "Delete",
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Delete", style = MaterialTheme.typography.labelLarge)
        }
    }
}