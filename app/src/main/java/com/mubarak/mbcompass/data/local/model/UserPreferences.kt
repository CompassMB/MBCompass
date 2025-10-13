// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.data.local.model

data class UserPreferences(
    val theme: String,
    val isTrueNorthEnabled: Boolean = false,
    // later we add more properties
) {

    companion object {
        const val KEY_THEME = "theme"
        const val TRUE_NORTH = "true_north"
    }
}
