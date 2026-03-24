// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.navigation

import androidx.annotation.StringRes
import com.mubarak.mbcompass.R
import kotlinx.serialization.Serializable

@Serializable
data object NavigationRoute

@Serializable
data class MapRoute(val trackUri: String? = null)

@Serializable
data object TracksRoute

@Serializable
data class TrackRoute(val trackUri: String?)

@Serializable
data object SettingsRoute

data class TopLevelRoute<T : Any>(
    val route: T,
    @StringRes val label: Int,
    val icon: Int
)

val TopLevelDestination = listOf(
    TopLevelRoute(NavigationRoute, R.string.navigation, R.drawable.ic_nav_24px),
    TopLevelRoute(MapRoute(),      R.string.map,        R.drawable.ic_map_24px),
    TopLevelRoute(TracksRoute,      R.string.tracks,        R.drawable.ic_tracks_24px),
    TopLevelRoute(SettingsRoute,   R.string.settings,   R.drawable.ic_settings_24px),
)

