// SPDX-License-Identifier: GPL-3.0-or-later
/*
 * Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
 * This project is licensed under GPL-3.0. Any derivative work must keep the same license,
 * retain this copyright notice, and provide proper attribution.
 */

package com.mubarak.mbcompass.core.permission

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.mubarak.mbcompass.R

class PermissionHandler(
    val context: Context,
    val shouldShowRationale: (permission: String) -> Boolean,
) {

    fun requestLocationPermission(
        launcher: ActivityResultLauncher<String>,
        onGranted: () -> Unit,
        onDenied: () -> Unit,
    ) {
        when {
            hasLocationPermission() -> onGranted()

            shouldShowRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showLocationRationaleDialog(
                    onPositive = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                )
            }

            else -> {
                showLocationEducationDialog(
                    onPositive = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    onNegative = onDenied
                )
            }
        }
    }

    fun requestNotificationPermission(
        launcher: ActivityResultLauncher<String>,
        onGranted: () -> Unit,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onGranted()
            return
        }
        when {
            hasNotificationPermission() -> onGranted()

            shouldShowRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                showNotificationRationaleDialog(
                    onPositive = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                )
            }

            else -> {
                showNotificationEducationDialog(
                    onPositive = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    onNegative = {}
                )
            }
        }
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    fun openAppSettings() {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    fun openLocationSettings() {
        context.startActivity(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }


    private fun showLocationEducationDialog(onPositive: () -> Unit, onNegative: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(R.string.location_permission_title)
            .setMessage(R.string.location_permission_message)
            .setIcon(R.drawable.location_icon24px)
            .setPositiveButton(R.string.grant_permission) { _, _ -> onPositive() }
            .setNegativeButton(R.string.not_now) { _, _ -> onNegative() }
            .setCancelable(false)
            .show()
    }

    private fun showLocationRationaleDialog(onPositive: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(R.string.location_permission_required)
            .setMessage(R.string.location_permission_rationale)
            .setIcon(R.drawable.location_icon24px)
            .setPositiveButton(R.string.try_again) { _, _ -> onPositive() }
            .setNeutralButton(R.string.open_settings) { _, _ -> openAppSettings() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showNotificationEducationDialog(onPositive: () -> Unit, onNegative: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(R.string.notification_permission_title)
            .setMessage(R.string.notification_permission_message)
            .setPositiveButton(R.string.grant_permission) { _, _ -> onPositive() }
            .setNegativeButton(R.string.not_now) { _, _ -> onNegative() }
            .setCancelable(false)
            .show()
    }

    private fun showNotificationRationaleDialog(onPositive: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(R.string.notification_permission_required)
            .setMessage(R.string.notification_permission_rationale)
            .setPositiveButton(R.string.try_again) { _, _ -> onPositive() }
            .setNeutralButton(R.string.open_settings) { _, _ -> openAppSettings() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}