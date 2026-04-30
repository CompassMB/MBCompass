// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.features.map

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentActivity
import com.mubarak.mbcompass.databinding.FragmentMapContainerBinding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    trackUri: String? = null,
)  {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // add this to remove extra spaces

    ) {
        val activity = LocalActivity.current as FragmentActivity
        MapContainerView(
            modifier = Modifier.padding(it),
            fragmentActivity = activity,
            trackUri = trackUri
        )
    }

}

@Composable
fun MapContainerView(
    modifier: Modifier = Modifier,
    trackUri: String?= null,
    fragmentActivity: FragmentActivity) {

    // https://stackoverflow.com/questions/74218090/how-to-access-getsupportfragmentmanager-in-componentactivity
    AndroidViewBinding(FragmentMapContainerBinding::inflate, modifier = modifier) {
        val fragmentManager = fragmentActivity.supportFragmentManager
        val existingFragment = fragmentManager.findFragmentById(fragmentContainerView.id) as? MapFragment

        // Replace if no fragment exists, OR if trackUri has changed
        val existingUri = existingFragment?.arguments?.getString(MapFragment.ARG_TRACK_URI)
        val uriChanged = existingUri != trackUri

        if (existingFragment == null || uriChanged) {
            fragmentManager.beginTransaction()
                .replace(fragmentContainerView.id, MapFragment.newInstance(trackUri))
                .commitAllowingStateLoss()
        }
    }
}
