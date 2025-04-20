// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.mubarak.mbcompass.ui.CompassNavGraph
import com.mubarak.mbcompass.ui.theme.MBCompassTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MBCompassTheme {
                CompassNavGraph()
            }
        }
    }
}