package com.mubarak.mbcompass.ui.compass

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.mubarak.mbcompass.utils.CardinalDirection
import kotlin.math.roundToInt

@Composable
fun CompassApp(context: Context, navigateToMapScreen: () -> Unit) {
    val androidSensorEventListener = AndroidSensorEventListener(context)

    KeepScreenOn()
    var degreeIn by remember {
        mutableFloatStateOf(0F)
    }
    var magnetic by remember {
        mutableFloatStateOf(0F)
    }
    Scaffold(
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = navigateToMapScreen,
            ) {
                Icon(painterResource(R.drawable.map_fill_icon_24px), contentDescription = stringResource(R.string.current_location))
            }
        }
    ) { innerPadding ->
        RegisterListener(
            lifecycleEventObserver = LocalLifecycleOwner.current,
            listener = androidSensorEventListener,
            degree = { degreeIn = it },
            mStrength = { magnetic = it })
        MBCompass(
            modifier = Modifier.padding(innerPadding),
            degreeIn = degreeIn,
            magneticStrength = magnetic
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MBCompass(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    degreeIn: Float,
    magneticStrength: Float,
) {
    val azimuthState by viewModel.azimuth.collectAsStateWithLifecycle()
    val strength by viewModel.strength.collectAsStateWithLifecycle()

    LaunchedEffect(degreeIn, magneticStrength) {
        viewModel.updateAzimuth(degreeIn)
        viewModel.updateMagneticStrength(magneticStrength)
    }

    val degree = azimuthState.roundToInt()
    val direction = remember(azimuthState) {
        CardinalDirection.getDirectionFromAzimuth(azimuthState)
    }
    val strengthRounded = strength.roundToInt()

    FlowColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.mbcompass_rose),
            contentDescription = stringResource(id = R.string.compass),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            modifier = Modifier
                .size(400.dp) // TODO: FIXME set dynamic size
                .padding(16.dp)
                .graphicsLayer {
                    rotationZ = -degree.toFloat()
                }
        )


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
                text = "Magnetic Strength $strengthRounded µT",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
fun RegisterListener(
    lifecycleEventObserver: LifecycleOwner,
    listener: AndroidSensorEventListener,
    degree: (Float) -> Unit = {},
    mStrength: (Float) -> Unit,
) {

    DisposableEffect(lifecycleEventObserver) {
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

    val list = object : AndroidSensorEventListener.AzimuthValueListener {
        override fun onAzimuthValueChange(degree: Float) {
            degree(degree)
        }

        override fun onMagneticStrengthChange(strengthInUt: Float) {
            mStrength(strengthInUt)
        }
    }

    listener.setAzimuthListener(list)
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