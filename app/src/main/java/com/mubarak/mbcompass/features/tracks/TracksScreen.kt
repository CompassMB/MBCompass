package com.mubarak.mbcompass.features.tracks

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.features.tracks.model.TrackListModel
import com.mubarak.mbcompass.navigation.MapRoute
import com.mubarak.mbcompass.utils.DateTimeFormatter
import com.mubarak.mbcompass.utils.LengthUnitHelper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
    modifier: Modifier = Modifier,
    viewModel: TracksViewModel = hiltViewModel<TracksViewModel>(),
    navController: NavHostController,
    onTrackClick: (TrackListModel) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (uiState.tracks.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.calculateTotals() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.dark_mode_icon24px),
                        contentDescription = "Statistics"
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.tracks))
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.tracks.isEmpty() -> {
                TracksOnboarding(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                TracksList(
                    tracks = uiState.tracks,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onTrackClick = { track ->
                        navController.navigate(MapRoute(trackUri = track.trackUriString))
                    },

                    onStarClick = { track ->
                        viewModel.toggleStarred(track.trackId)
                    },
                    onDeleteTrack = { track ->
                        viewModel.deleteTrack(track.trackId)
                    }
                )
            }
        }
    }
}


@Composable
private fun TracksList(
    tracks: List<TrackListModel>,
    modifier: Modifier = Modifier,
    onTrackClick: (TrackListModel) -> Unit,
    onStarClick: (TrackListModel) -> Unit,
    onDeleteTrack: (TrackListModel) -> Unit
) {
    var trackToDelete by remember { mutableStateOf<TrackListModel?>(null) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = tracks,
            key = { it.trackId }
        ) { track ->
            TrackListItem(
                track = track,
                onTrackClick = { onTrackClick(track) },
                onStarClick = { onStarClick(track) },
                onDeleteRequest = { trackToDelete = track }
            )
        }
    }

    // delete confirmation dialog
    trackToDelete?.let { track ->
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            title = { Text(stringResource(R.string.delete_track_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.delete_track_message,
                        track.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTrack(track)
                        trackToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}


@Composable
private fun TrackListItem(
    track: TrackListModel,
    onTrackClick: () -> Unit,
    onStarClick: () -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTrackClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = DateTimeFormatter.formatDateTimeString(track.date),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TrackStat(
                        icon = R.drawable.ic_distance_24px,
                        value = LengthUnitHelper.convertDistanceToString(track.length)
                    )

                    TrackStat(
                        icon = R.drawable.ic_duration24px,
                        value = DateTimeFormatter.formatDurationTime(track.duration)
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more24px),
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (track.starred) stringResource(R.string.unstar_track)
                                else stringResource(R.string.star_track)
                            )
                        },
                        onClick = {
                            onStarClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(
                                    if (track.starred) R.drawable.star_fill_24px
                                    else R.drawable.star_24px
                                ),
                                contentDescription = null
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            onDeleteRequest()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.delete_24px),
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackStat(
    @DrawableRes icon: Int,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TracksOnboarding(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_tracks_24px),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.no_tracks_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.no_tracks_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
