// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.compass

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mubarak.mbcompass.MainViewModel
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.sensor.AndroidSensorEventListener
import com.mubarak.mbcompass.sensor.SensorViewModel
import com.mubarak.mbcompass.ui.settings.SettingsViewModel
import com.mubarak.mbcompass.utils.Azimuth
import com.mubarak.mbcompass.utils.CardinalDirection
import com.mubarak.mbcompass.utils.getMagneticDeclination
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassApp(
    sensorViewModel: SensorViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    navigateToMap: () -> Unit,
    navigateToSettings: () -> Unit
) {

    val context = LocalContext.current

    var sensorEventListener by remember { mutableStateOf<AndroidSensorEventListener?>(null) }

    val sensorIconState by sensorViewModel.sensorStatusIcon.collectAsStateWithLifecycle()

    val dialogState by sensorViewModel.accuracyAlertDialogState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        sensorEventListener = AndroidSensorEventListener(
            context = context,
            onAccuracyUpdate = { accuracy ->
                sensorViewModel.updateSensorAccuracy(accuracy)
            },
        )
    }

    // Show AlertDialog based on dialogState
    if (dialogState.show && dialogState.accuracyForDialog != null) {
        ShowAccuracyAlertDialog(
            context = context, accuracy = dialogState.accuracyForDialog!!, onDismiss = {
                sensorViewModel.accuracyDialogDismissed()
            })
    }

    KeepScreenOn()
    var degreeIn by remember { mutableStateOf<Azimuth>(Azimuth(0F)) }
    var magnetic by remember { mutableFloatStateOf(0F) }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), topBar = {
        TopAppBar(title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.W700,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF45DEA4),
                            Color(0xFF45CCDE)
                        )
                    )
                )
            )
        }, actions = {
            IconButton(onClick = { sensorViewModel.sensorStatusIconClicked() }) {
                Icon(
                    painter = painterResource(id = sensorIconState.iconResId),
                    contentDescription = sensorIconState.contentDescription
                )
            }
            IconButton(onClick = navigateToSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings_content_description)
                )
            }
        })
    }, floatingActionButton = {
        SmallFloatingActionButton(
            onClick = navigateToMap, modifier = Modifier.navigationBarsPadding()
        ) {
            Icon(
                painterResource(R.drawable.map_fill_icon_24px),
                contentDescription = stringResource(R.string.map)
            )
        }
    }) { innerPadding ->
        val context = LocalContext.current

        sensorEventListener?.let { listener ->
            RegisterListener(
                lifecycleEventObserver = LocalLifecycleOwner.current,
                listener = listener,
                settingsViewModel = settingsViewModel,
                context = context,
                sensorViewModel = sensorViewModel,
                degree = { degreeIn = it },
                mStrength = { magnetic = it })
        }
        MBCompass(
            modifier = Modifier.padding(innerPadding),
            degreeIn = degreeIn,
            magneticStrength = magnetic
        )
    }
}

@Composable
fun MBCompass(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    sensorViewModel: SensorViewModel = viewModel(),
    degreeIn: Azimuth,
    magneticStrength: Float,
) {
    val azimuthState by viewModel.azimuth.collectAsStateWithLifecycle()
    val strength by viewModel.strength.collectAsStateWithLifecycle()

    val location by sensorViewModel.location.collectAsStateWithLifecycle()
    val trueNorthEnabled by sensorViewModel.trueNorthEnabled.collectAsState()

    LaunchedEffect(
        degreeIn, magneticStrength
    ) { // if something changes in this case degreeIn, magneticStrength -> Notify to the VM
        viewModel.updateAzimuth(degreeIn)
        viewModel.updateMagneticStrength(magneticStrength)
    }

    val degree by remember {
        derivedStateOf { azimuthState.wrapAzimuth(azimuthState.roundedDegrees) }
    }

    val direction by remember {
        derivedStateOf { CardinalDirection.getDirectionFromAzimuth(degree) }
    }

    val strengthRounded by remember {
        derivedStateOf { strength.roundToInt() }
    }

    val context = LocalContext.current
    remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        FlowColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center
        ) {

            if (trueNorthEnabled) {
                Log.e(TAG, "True North Enabled")
                val magneticDec = getMagneticDeclination(context = context, azimuthState)
                val trueAzimuth = degreeIn.add(magneticDec)
                setAzimuth(trueAzimuth, mainViewModel = viewModel)
            } else {
                Log.e(TAG, "True North Disabled")
                setAzimuth(degreeIn, mainViewModel = viewModel)
            }

            CompassView(azimuth = { degreeIn })

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "$degree°",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = stringResource(id = direction.dirName),
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = if (trueNorthEnabled) "True North" else "Magnetic North",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "Magnetic Strength $strengthRounded µT",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (trueNorthEnabled && location == null) {
                Button(
                    onClick = {
                        val hasFineLocation = ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        val hasCoarseLocation = ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (hasFineLocation || hasCoarseLocation) {
                            Log.d(TAG, "MBCompass: Location Permission granted ")

                            val androidLocationManager = AndroidLocationManager(context) { loc ->
                                sensorViewModel.provideLocation(loc)
                            }
                            androidLocationManager.registerLocationListener()

                            // No location permission, show Alertdialog
                            if (!LocationManagerCompat.isLocationEnabled(
                                    context.getSystemService(
                                        Context.LOCATION_SERVICE
                                    ) as android.location.LocationManager
                                )
                            ) {
                                Log.d(TAG, "MBCompass: Location is disabled ")

                                locationRequestDialog(
                                    title = R.string.location_disabled,
                                    message = R.string.location_disabled_rationale,
                                    actionIntent = Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                                    context = context
                                )
                            }

                        } else {
                            locationRequestDialog(
                                title = R.string.permission_required,
                                message = R.string.permission_rationale,
                                actionIntent = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                context = context
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.icon_support_24), null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.reload_location))
                }
            }
        }
    }
}

private fun getMagneticDeclination(context: Context, azimuth: Azimuth): Float {
    var location: Location = Location("")
    AndroidLocationManager(context, location = { location = it })

    Log.d(TAG, "getMagneticDeclination: Location:$location")
    return azimuth.add(getMagneticDeclination(location)).roundedDegrees
}

@Composable
fun CompassView(
    azimuth: () -> Azimuth,
) {
    val image = rememberVectorPainter(image = ImageVector.vectorResource(R.drawable.mbcompass_rose))

    Box(
        modifier = Modifier
            .size(400.dp)
            .padding(16.dp)
            .graphicsLayer {
                rotationZ = -azimuth().roundedDegrees
            }, contentAlignment = Alignment.Center
    ) {
        Image(
            painter = image,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxSize()
        )
    }
}

internal fun setAzimuth(azimuth: Azimuth, mainViewModel: MainViewModel) {
    Log.v(TAG, "Azimuth $azimuth")
    mainViewModel.updateAzimuth(azimuth)
}

@SuppressLint("MissingPermission")
@Composable
fun RegisterListener(
    lifecycleEventObserver: LifecycleOwner,
    sensorViewModel: SensorViewModel,
    settingsViewModel: SettingsViewModel,
    listener: AndroidSensorEventListener,
    context: Context,
    degree: (Azimuth) -> Unit = {},
    mStrength: (Float) -> Unit,
) {

    val androidLocationManager = remember {
        AndroidLocationManager(context) { location ->
            sensorViewModel.provideLocation(location)
        }
    }

    val settingsState by settingsViewModel.uiState.collectAsState()
    val trueNorthState = settingsState.isTrueNorthEnabled

    LaunchedEffect(trueNorthState) {
        sensorViewModel.setTrueNorthState(trueNorthState)
    }

    val location by sensorViewModel.location.collectAsState()
    LaunchedEffect(trueNorthState, location) {

        if (trueNorthState && location == null) {
            Log.d(TAG, "RegisterListener: Register LM")

            androidLocationManager.registerLocationListener()
        }
    }

    Log.d(TAG, "TN State: ${settingsViewModel.uiState.collectAsState().value.isTrueNorthEnabled}")

    DisposableEffect(listener, lifecycleEventObserver) {
        val azimuthListener = object : AndroidSensorEventListener.AzimuthValueListener {
            override fun onAzimuthValueChange(degree: Azimuth) = degree(degree)
            override fun onMagneticStrengthChange(strengthInUt: Float) = mStrength(strengthInUt)
        }
        listener.setAzimuthListener(azimuthListener)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> listener.registerSensor()
                Lifecycle.Event.ON_PAUSE -> listener.unregisterSensorListener()
                else -> {}
            }
        }
        lifecycleEventObserver.lifecycle.addObserver(observer)

        onDispose {
            lifecycleEventObserver.lifecycle.removeObserver(observer)
            listener.unregisterSensorListener()
        }
    }
}

private fun locationRequestDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    actionIntent: String,
    context: Context
) {
    AlertDialog.Builder(context)
        .setTitle(title)
        .setIcon(R.drawable.error_icon24px)
        .setMessage(context.getString(message))
        .setPositiveButton(R.string.ok_button) { dialog, _ -> dialog.dismiss() }
        .setNegativeButton(R.string.settings) { _, _ ->
            val intent = if (actionIntent == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                Intent(actionIntent, Uri.fromParts("package", context.packageName, null))
            } else {
                Intent(actionIntent)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        .show()
}


@Composable
fun ShowAccuracyAlertDialog(context: Context, accuracy: Int, onDismiss: () -> Unit) {
    val accuracyString = when (accuracy) {
        SensorManager.SENSOR_STATUS_UNRELIABLE -> context.getString(R.string.accuracy_unreliable)
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> context.getString(R.string.accuracy_low)
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> context.getString(R.string.accuracy_medium)
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> context.getString(R.string.accuracy_high)
        else -> context.getString(R.string.accuracy_unknown)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.calibration_title)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.figure_8_ptn),
                    contentDescription = stringResource(R.string.figure_8_pattern),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(context.getString(R.string.calibration_required_message, accuracyString))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(context.getString(R.string.ok_button))
            }
        })
}

@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}