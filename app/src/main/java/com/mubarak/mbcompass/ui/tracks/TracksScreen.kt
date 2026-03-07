// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.tracks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mubarak.mbcompass.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
    modifier: Modifier = Modifier
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.tracks))
                }
            )
        }
    ) { paddingValues ->
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Text("Tracks")
        }
    }
}

