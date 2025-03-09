package com.mubarak.mbcompass

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.mubarak.mbcompass.ui.CompassNavGraph
import com.mubarak.mbcompass.ui.theme.MBCompassTheme
import org.maplibre.android.MapLibre

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        setContent {
            MBCompassTheme {
                CompassNavGraph()
            }
        }
    }
}