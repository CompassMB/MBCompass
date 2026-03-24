// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.features.track

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.mubarak.mbcompass.R
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentActivity
import com.mubarak.mbcompass.databinding.FragmentTrackContainerBinding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    trackUri: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.track_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_24px),
                            contentDescription = stringResource(R.string.nav_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val activity = LocalActivity.current as FragmentActivity

        TrackContainerView(
            modifier = Modifier.padding(paddingValues),
            fragmentActivity = activity,
            trackUri = trackUri
        )
    }
}


@Composable
private fun TrackContainerView(
    modifier: Modifier = Modifier,
    trackUri: String,
    fragmentActivity: FragmentActivity
) {
    AndroidViewBinding(
        factory = FragmentTrackContainerBinding::inflate,
        modifier = modifier
    ) {
        val fragmentManager = fragmentActivity.supportFragmentManager
        val existingFragment = fragmentManager.findFragmentById(
            trackFragmentContainer.id
        ) as? TrackViewFragment

        // Get existing track URI
        val existingUri = existingFragment?.arguments?.getString(
            TrackViewFragment.ARG_TRACK_URI
        )

        // Replace fragment if URI changed or no fragment exists
        val uriChanged = existingUri != trackUri

        if (existingFragment == null || uriChanged) {
            fragmentManager.beginTransaction()
                .replace(
                    trackFragmentContainer.id,
                    TrackViewFragment.newInstance(trackUri)
                )
                .commitAllowingStateLoss()
        }
    }
}