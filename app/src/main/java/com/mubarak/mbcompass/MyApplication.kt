// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass

import android.app.Application
import com.mubarak.mbcompass.data.AppPreferences.initPreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp: Application(){
    override fun onCreate() {
        super.onCreate()
        this.initPreferences()
    }
}