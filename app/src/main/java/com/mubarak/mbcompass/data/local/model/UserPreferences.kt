// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.data.local.model

data class UserPreferences(
    val theme: String,
    val isTrueDarkThemeEnabled: Boolean = false,
    val isTrueNorthEnabled: Boolean = false,
) {

    companion object {
        const val KEY_THEME = "theme"
        const val TRUE_DARK = "true_dark"
        const val TRUE_NORTH = "true_north"
    }
}
