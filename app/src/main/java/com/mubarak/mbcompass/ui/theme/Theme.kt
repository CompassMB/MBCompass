// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui.theme

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mubarak.mbcompass.ui.settings.SettingsViewModel
import com.mubarak.mbcompass.utils.ThemeConfig

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private fun mbColorScheme(
    context: Context,
    isDarkTheme: Boolean,
    isDynamicTheme: Boolean,
    isAmoledTheme: Boolean
): ColorScheme {
    if (isDynamicTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDarkTheme) {
            if (isAmoledTheme) {
                return dynamicDarkColorScheme(context).copy(
                    background = Color.Black,
                    surface = Color.Black
                )
            }

            return dynamicDarkColorScheme(context)
        } else {
            return dynamicLightColorScheme(context)
        }
    } else if (isDarkTheme) {
        if (isAmoledTheme) {
            return trueBlackColorScheme(darkScheme)
        }
        return darkScheme
    } else {
        return lightScheme
    }
}

fun trueBlackColorScheme(darkScheme: ColorScheme) = darkScheme.copy(
    surface = Color.Black,
    background = Color.Black,
    surfaceVariant = Color.Black,
    surfaceTint = Color.Black,
    surfaceContainer = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerHigh = Color.Black,
    surfaceContainerHighest = Color.Black,
)


@Composable
fun MBCompassTheme(
    darkTheme: Boolean,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    uiState: SettingsViewModel.SettingsUiState,
    content: @Composable () -> Unit
) {
    when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    val isDark = when (uiState.theme) {
        ThemeConfig.FOLLOW_SYSTEM.prefName -> darkTheme
        ThemeConfig.DARK.prefName -> true
        ThemeConfig.LIGHT.prefName -> false
        else -> darkTheme
    }
    val isAmoled = uiState.isTrueDarkThemeEnabled && isDark

    Log.d("Theme value", "MBCompassTheme: $isAmoled")
    val context = LocalContext.current
    MaterialTheme(
        colorScheme = mbColorScheme(context, darkTheme, dynamicColor, isAmoled),
        typography = Typography,
        content = content
    )
}