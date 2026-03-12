package com.mubarak.mbcompass.features.tracks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubarak.mbcompass.data.TrackRepository
import com.mubarak.mbcompass.features.tracks.model.TrackListModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<TrackListModel>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<TracksUiState> = combine(
        _tracks,
        _isLoading
    ) { tracks, isLoading ->
        TracksUiState(
            tracks = tracks,
            isLoading = isLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TracksUiState()
    )

    init {
        loadTracks()
    }

    fun loadTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tracklist = trackRepository.readTracklist()
                _tracks.value = tracklist.trackList.sortedByDescending { it.date }
            } catch (e: Exception) {
                _tracks.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTrack(trackId: Long) {
        viewModelScope.launch {
            try {
                trackRepository.deleteTrack(trackId)
                loadTracks()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleStarred(trackId: Long) {
        viewModelScope.launch {
            try {
                val tracklist = trackRepository.readTracklist()
                val track = tracklist.trackList.find { it.trackId == trackId }
                track?.let {
                    it.starred = !it.starred
                    trackRepository.saveTracklist(tracklist)
                    loadTracks()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun calculateTotals() {
        viewModelScope.launch {
            try {
                val tracklist = trackRepository.readTracklist()
                var totalDistance = 0f
                tracklist.trackList.forEach { track ->
                    totalDistance += track.length
                }
                tracklist.totalDistanceAll = totalDistance
                trackRepository.saveTracklist(tracklist)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class TracksUiState(
    val tracks: List<TrackListModel> = emptyList(),
    val isLoading: Boolean = false,
)

