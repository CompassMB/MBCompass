package com.mubarak.mbcompass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mubarak.mbcompass.ui.CompassNavGraph
import com.mubarak.mbcompass.ui.compass.CompassApp
import com.mubarak.mbcompass.ui.theme.MBCompassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MBCompassTheme {
              CompassNavGraph()
            }
        }
    }
}