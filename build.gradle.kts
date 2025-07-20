// SPDX-License-Identifier: GPL-3.0-or-later

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlinSymbolProcessing)
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false

}