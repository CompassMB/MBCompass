// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mubarak.mbcompass.R
import kotlinx.coroutines.launch

object FragmentNotifications {

    fun attachSnackbarHost(
        composeView: ComposeView,
        bottomOffsetDp: Int
    ): SnackbarHostState {
        val snackbarHostState = SnackbarHostState()

        composeView.setContent {
            SnackbarContainer(
                snackBarHostState = snackbarHostState,
                bottomOffsetDp = bottomOffsetDp
            )
        }

        return snackbarHostState
    }

    @Composable
    private fun SnackbarContainer(
        snackBarHostState: SnackbarHostState,
        bottomOffsetDp: Int
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomOffsetDp.dp)
            )
        }
    }

    fun showSnackbar(
        fragment: Fragment,
        snackbarHostState: SnackbarHostState,
        message: String,
        actionText: String? = null,
        onAction: (() -> Unit)? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        fragment.lifecycleScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionText,
                withDismissAction = true,
                duration = duration
            )

            if (result == SnackbarResult.ActionPerformed) {
                onAction?.invoke()
            }
        }
    }

    fun showLocationOff(
        fragment: Fragment,
        snackbarHostState: SnackbarHostState,
        onEnableClick: () -> Unit
    ) {
        showSnackbar(
            fragment = fragment,
            snackbarHostState = snackbarHostState,
            message = fragment.getString(R.string.location_disabled_simple),
            actionText = fragment.getString(R.string.enable),
            onAction = onEnableClick,
            duration = SnackbarDuration.Short
        )
    }

    fun showToast(
        fragment: Fragment,
        message: String
    ) {
        Toast
            .makeText(fragment.requireContext(), message, Toast.LENGTH_SHORT)
            .show()
    }
}