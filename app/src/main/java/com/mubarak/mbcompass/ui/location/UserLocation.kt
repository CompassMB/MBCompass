package com.mubarak.mbcompass.ui.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UserLocation(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()){
        Text("Show User Location")
    }
}