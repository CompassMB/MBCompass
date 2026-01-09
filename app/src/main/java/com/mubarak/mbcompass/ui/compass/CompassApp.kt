// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.compass

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
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
import com.mubarak.mbcompass.utils.KeepScreenOn
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassApp(
    sensorViewModel: SensorViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>(),
    navigateToMap: () -> Unit,
    navigateToSettings: () -> Unit
) {

    val context = LocalContext.current

    var sensorEventListener by remember { mutableStateOf<AndroidSensorEventListener?>(null) }

    val sensorIconState by sensorViewModel.sensorStatusIcon.collectAsStateWithLifecycle()

    val dialogState by sensorViewModel.accuracyAlertDialogState.collectAsStateWithLifecycle()

    val androidLocationManager = remember {
        AndroidLocationManager(context) { location ->
            sensorViewModel.provideLocation(location)
        }
    }

    LaunchedEffect(Unit) {
        sensorEventListener = AndroidSensorEventListener(
            context = context,
            sensorViewModel = sensorViewModel,
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
    var magneticStrength by remember { mutableFloatStateOf(0F) }

    val azimuthSensor by mainViewModel.azimuth.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()


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
                    painterResource(R.drawable.settings_24px),
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

        sensorEventListener?.let { listener ->
            RegisterListener(
                lifecycleOwner = LocalLifecycleOwner.current,
                listener = listener,
                androidLocationManager = androidLocationManager,
                sensorViewModel = sensorViewModel,
                settingsUiState = settingsState,
                mainViewModel = mainViewModel,
                mStrength = { magneticStrength = it })
        }
        MBCompass(
            modifier = Modifier.padding(innerPadding),
            degreeIn = azimuthSensor,
            androidLocationManager = androidLocationManager,
            magneticStrength = magneticStrength
        )
    }
}

@Composable
fun MBCompass(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    sensorViewModel: SensorViewModel = viewModel(),
    androidLocationManager: AndroidLocationManager,
    degreeIn: Azimuth,
    magneticStrength: Float,
) {
    val azimuthState by viewModel.azimuth.collectAsStateWithLifecycle()
    val strength by viewModel.strength.collectAsStateWithLifecycle()

    val location by sensorViewModel.location.collectAsStateWithLifecycle()
    val trueNorthEnabled by sensorViewModel.trueNorthEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(
        degreeIn, magneticStrength
    ) { // if something changes in this case degreeIn, magneticStrength -> Notify to the VM
        viewModel.updateAzimuth(degreeIn)
        viewModel.updateMagneticStrength(magneticStrength)
    }

    val degree by remember {
        derivedStateOf { azimuthState.roundedDegrees }
    }

    val direction by remember {
        derivedStateOf { CardinalDirection.getDirectionFromAzimuth(degree) }
    }

    val strengthRounded by remember {
        derivedStateOf { strength.roundToInt() }
    }

    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        FlowColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center
        ) {
            CompassView(azimuth = degreeIn)

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
                    text = if (trueNorthEnabled) stringResource(R.string.true_north) else stringResource(
                        R.string.magnetic_north
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = stringResource(R.string.magnetic_strength,strengthRounded),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (trueNorthEnabled && location == null) {
                Button(
                    onClick = {
                        handleLocationRequest(context, androidLocationManager)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) {
                    LocationRequestProgress()
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.reload_location))
                }
            }
        }
    }
}

@Composable
fun LocationRequestProgress(modifier: Modifier = Modifier) {

    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

private fun handleLocationRequest(
    context: Context,
    androidLocationManager: AndroidLocationManager
) {
    val hasFineLocation = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasCoarseLocation = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (hasFineLocation || hasCoarseLocation) {
        Log.d("CompassApp", "Location Permission granted")

        androidLocationManager.registerLocationListener()

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE)
                as android.location.LocationManager

        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            Log.d("CompassApp", "Location is disabled")
            locationRequestDialog(
                title = R.string.location_disabled,
                message = R.string.location_disabled_rationale,
                actionIntent = Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                context = context
            )
        }
    } else {
        // No location permission, show AlertDialog
        locationRequestDialog(
            title = R.string.permission_required,
            message = R.string.permission_rationale,
            actionIntent = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            context = context
        )
    }
}

@Composable
fun CompassView(
    azimuth: Azimuth,
    modifier: Modifier = Modifier,
) {
    val targetRotation = azimuth.roundedDegrees

    // shortest rotation path logic
    var previousRotation by remember { mutableFloatStateOf(targetRotation) }
    var adjustedRotation by remember { mutableFloatStateOf(targetRotation) }

    LaunchedEffect(targetRotation) {
        val diff = targetRotation - previousRotation
        adjustedRotation += when {
            diff > 180 -> diff - 360
            diff < -180 -> diff + 360
            else -> diff
        }
        previousRotation = targetRotation
    }

    val animatedRotation by animateFloatAsState(
        targetValue = -adjustedRotation, // negative to rotate compass needle correctly
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CompassRotation"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp)
            .graphicsLayer {
                rotationZ = animatedRotation
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.mbcompass_rose),
            contentDescription = stringResource(R.string.compass_rose),
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun RegisterListener(
    lifecycleOwner: LifecycleOwner,
    androidLocationManager: AndroidLocationManager,
    sensorViewModel: SensorViewModel,
    settingsUiState: SettingsViewModel.SettingsUiState,
    listener: AndroidSensorEventListener,
    mainViewModel: MainViewModel,
    mStrength: (Float) -> Unit,
) {

    val trueNorthState = settingsUiState.isTrueNorthEnabled

    LaunchedEffect(trueNorthState) {
        sensorViewModel.setTrueNorthState(trueNorthState)
    }

    val location by sensorViewModel.location.collectAsStateWithLifecycle()
    LaunchedEffect(trueNorthState, location) {

        if (trueNorthState && location == null) {
            Log.d(TAG, "RegisterListener: Register LM")

            androidLocationManager.registerLocationListener()
        }
    }


    DisposableEffect(listener, lifecycleOwner) {
        val azimuthListener = object : AndroidSensorEventListener.AzimuthValueListener {
            override fun onAzimuthValueChange(degree: Azimuth) {
                mainViewModel.updateAzimuth(degree)
            }

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
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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