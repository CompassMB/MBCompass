// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.compass

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.hardware.SensorManager
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import com.mubarak.mbcompass.utils.CardinalDirection
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassApp(
    sensorViewModel: SensorViewModel = viewModel(),
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
    var degreeIn by remember { mutableFloatStateOf(0F) }
    var magnetic by remember { mutableFloatStateOf(0F) }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), topBar = {
        TopAppBar(title = {
            Text(
                text = "MBCompass",
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
        sensorEventListener?.let { listener ->
            RegisterListener(
                lifecycleEventObserver = LocalLifecycleOwner.current,
                listener = listener, // Pass the correctly scoped listener
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
    degreeIn: Float,
    magneticStrength: Float,
) {
    val azimuthState by viewModel.azimuth.collectAsStateWithLifecycle()
    val strength by viewModel.strength.collectAsStateWithLifecycle()

    LaunchedEffect(
        degreeIn, magneticStrength
    ) { // if something changes in this case degreeIn, magneticStrength -> notify to the vm
        viewModel.updateAzimuth(degreeIn)
        viewModel.updateMagneticStrength(magneticStrength)
    }

    val degree by remember {
        derivedStateOf { azimuthState.roundToInt() }
    }

    val direction by remember {
        derivedStateOf { CardinalDirection.getDirectionFromAzimuth(azimuthState) }
    }

    val strengthRounded by remember {
        derivedStateOf { strength.roundToInt() }
    }

    FlowColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center
    ) {

        CompassView(azimuth = { degree })

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "$degree°", style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = stringResource(id = direction.dirName),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Magnetic Strength $strengthRounded µT",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CompassView(
    azimuth: () -> Int,
) {
    val image = rememberVectorPainter(image = ImageVector.vectorResource(R.drawable.mbcompass_rose))

    Box(
        modifier = Modifier
            .size(400.dp)
            .padding(16.dp)
            .graphicsLayer {
                rotationZ = -azimuth().toFloat()
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


@Composable
fun RegisterListener(
    lifecycleEventObserver: LifecycleOwner,
    listener: AndroidSensorEventListener,
    degree: (Float) -> Unit = {},
    mStrength: (Float) -> Unit,
) {
    DisposableEffect(listener, lifecycleEventObserver) {
        val azimuthListener = object : AndroidSensorEventListener.AzimuthValueListener {
            override fun onAzimuthValueChange(degree: Float) {
                degree(degree)
            }

            override fun onMagneticStrengthChange(strengthInUt: Float) {
                mStrength(strengthInUt)
            }
        }
        listener.setAzimuthListener(azimuthListener) // Assuming setAzimuthListener is still present

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                listener.registerSensor()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                listener.unregisterSensorListener()
            }
        }
        lifecycleEventObserver.lifecycle.addObserver(observer)

        onDispose {
            lifecycleEventObserver.lifecycle.removeObserver(observer)
            listener.unregisterSensorListener()
        }
    }
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