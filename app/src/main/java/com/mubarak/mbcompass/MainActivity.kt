// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.mubarak.mbcompass.ui.CompassNavGraph
import com.mubarak.mbcompass.ui.theme.MBCompassTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    // FragmentActivity instead of ComponentActivity to support nested fragment which is require for our map
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MBCompassTheme {
                CompassNavGraph()
            }
        }
    }
}