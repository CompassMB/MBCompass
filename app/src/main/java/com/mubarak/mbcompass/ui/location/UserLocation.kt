// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.location

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentActivity
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.databinding.FragmentMapContainerBinding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLocation(
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(stringResource(R.string.map))
                },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back)
                        )
                    }
                },
            )
        }
    ) {
        val activity = LocalActivity.current as FragmentActivity
        UserLocationMapView(modifier = Modifier.padding(it), activity)
    }
}

@Composable
fun UserLocationMapView(modifier: Modifier = Modifier, fragmentActivity: FragmentActivity) {
    // https://stackoverflow.com/questions/74218090/how-to-access-getsupportfragmentmanager-in-componentactivity
    AndroidViewBinding(FragmentMapContainerBinding::inflate, modifier = modifier) {
        val fragmentManager = fragmentActivity.supportFragmentManager

        // Ensure the fragment is added only once
        if (fragmentManager.findFragmentById(fragmentContainerView.id) == null) {
            fragmentManager.beginTransaction()
                .replace(fragmentContainerView.id, MapFragment())
                .commit()
        }
    }
}
