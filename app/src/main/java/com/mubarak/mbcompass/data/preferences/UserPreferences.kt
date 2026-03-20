// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.data.preferences

data class UserPreferences(
    val theme: String,
    val isTrueDarkThemeEnabled: Boolean = false,
    val isTrueNorthEnabled: Boolean = false,
    val highAccuracy: Boolean = false,
    val lastLatitude: Double = 48.8583,  // Default: Paris
    val lastLongitude: Double = 2.2944,
    val lastZoomLevel: Double = 16.0
) {

    companion object {
        const val KEY_THEME = "theme"
        const val TRUE_DARK = "true_dark"
        const val TRUE_NORTH = "true_north"
        const val HIGH_ACCURACY = "high_accuracy"
        const val LAST_LATITUDE = "last_latitude"
        const val LAST_LONGITUDE = "last_longitude"
        const val LAST_ZOOM_LEVEL = "last_zoom_level"
    }
}