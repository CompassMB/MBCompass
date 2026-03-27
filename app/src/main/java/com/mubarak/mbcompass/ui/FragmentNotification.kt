// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mubarak.mbcompass.R

object FragmentNotifications {


    fun showToast(
        fragment: Fragment,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(fragment.requireContext(), message, duration).show()
    }

    // Show custom banner notification with optional action
    fun showBanner(
        fragment: Fragment,
        rootView: View,
        message: String,
        actionText: String? = null,
        onAction: (() -> Unit)? = null,
        duration: Long = 5000L
    ) {
        val context = fragment.requireContext()

        val banner = TextView(context).apply {
            text = if (actionText != null) "$message    [$actionText]" else message
            setTextColor(ContextCompat.getColor(context, R.color.white))
            setBackgroundColor(ContextCompat.getColor(context, R.color.brand_color))
            setPadding(32, 24, 32, 24)
            gravity = Gravity.CENTER
            textSize = 14f

            if (onAction != null) {
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    onAction()
                    (parent as? ViewGroup)?.removeView(this)
                }
            }
        }

        if (rootView is FrameLayout) {
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = 210
            }

            rootView.addView(banner, params)

            banner.postDelayed({
                (banner.parent as? ViewGroup)?.removeView(banner)
            }, duration)
        } else {
            // Fallback to Toast
            showToast(fragment, message, Toast.LENGTH_LONG)
        }
    }

    fun showLocationOff(
        fragment: Fragment,
        rootView: View,
        onEnableClick: () -> Unit
    ) {
        showBanner(
            fragment, rootView,
            "Location services are off",
            "ENABLE",
            onEnableClick,
            7000L
        )
    }
}