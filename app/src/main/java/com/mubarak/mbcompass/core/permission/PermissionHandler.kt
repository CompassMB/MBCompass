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
import androidx.fragment.app.Fragment
import com.mubarak.mbcompass.R

class PermissionHandler(
    private val fragment: Fragment
) {

    private val context: Context
        get() = fragment.requireContext()


    fun requestLocationPermission(
        launcher: ActivityResultLauncher<String>,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        when {
            // Already granted
            hasLocationPermission() -> {
                onGranted()
            }

            // Should show rationale (user denied before)
            fragment.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showLocationRationaleDialog(
                    onPositive = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    onNegative = onDenied
                )
            }

            // First time asking
            else -> {
                showLocationEducationDialog(
                    onPositive = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    onNegative = onDenied
                )
            }
        }
    }


    private fun showLocationEducationDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(R.string.location_permission_title)
            .setMessage(R.string.location_permission_message)
            .setIcon(R.drawable.location_icon24px)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                onPositive()
            }
            .setNegativeButton(R.string.not_now) { _, _ ->
                onNegative()
            }
            .setCancelable(false)
            .show()
    }


    // Rationale dialog
    private fun showLocationRationaleDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(R.string.location_permission_required)
            .setMessage(R.string.location_permission_rationale)
            .setIcon(R.drawable.location_icon24px)
            .setPositiveButton(R.string.try_again) { _, _ ->
                onPositive()
            }
            .setNeutralButton(R.string.open_settings) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }


    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    fun requestNotificationPermission(
        launcher: ActivityResultLauncher<String>,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Not needed on Android 12 and below
            onGranted()
            return
        }

        when {
            hasNotificationPermission() -> {
                onGranted()
            }

            fragment.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                showNotificationRationaleDialog(
                    onPositive = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    onNegative = onDenied
                )
            }

            else -> {
                showNotificationEducationDialog(
                    onPositive = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    onNegative = onDenied
                )
            }
        }
    }

    private fun showNotificationEducationDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(R.string.notification_permission_title)
            .setMessage(R.string.notification_permission_message)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                onPositive()
            }
            .setNegativeButton(R.string.not_now) { _, _ ->
                onNegative()
            }
            .setCancelable(false)
            .show()
    }

    private fun showNotificationRationaleDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(R.string.notification_permission_required)
            .setMessage(R.string.notification_permission_rationale)
            .setPositiveButton(R.string.try_again) { _, _ ->
                onPositive()
            }
            .setNeutralButton(R.string.open_settings) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required on older versions
        }
    }

    fun requestActivityRecognitionPermission(
        launcher: ActivityResultLauncher<String>,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Not needed on Android 9 and below
            onGranted()
            return
        }

        when {
            hasActivityRecognitionPermission() -> {
                onGranted()
            }

            fragment.shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) -> {
                showActivityRecognitionRationaleDialog(
                    onPositive = { launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION) },
                    onNegative = onDenied
                )
            }

            else -> {
                showActivityRecognitionEducationDialog(
                    onPositive = { launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION) },
                    onNegative = onDenied
                )
            }
        }
    }

    private fun showActivityRecognitionEducationDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(R.string.activity_recognition_permission_title)
            .setMessage(R.string.activity_recognition_permission_message)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                onPositive()
            }
            .setNegativeButton(R.string.not_now) { _, _ ->
                onNegative()
            }
            .setCancelable(false)
            .show()
    }

    private fun showActivityRecognitionRationaleDialog(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(R.string.activity_recognition_permission_required)
            .setMessage(R.string.activity_recognition_permission_rationale)
            .setPositiveButton(R.string.try_again) { _, _ ->
                onPositive()
            }
            .setNeutralButton(R.string.open_settings) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun hasActivityRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required on older versions
        }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }


    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}