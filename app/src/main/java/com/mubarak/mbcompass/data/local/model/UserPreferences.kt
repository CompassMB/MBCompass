// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.data.local.model

data class UserPreferences(
    val theme: String,
    // later we add more properties
) {

    companion object {
        const val KEY_THEME = "theme"
    }
}
