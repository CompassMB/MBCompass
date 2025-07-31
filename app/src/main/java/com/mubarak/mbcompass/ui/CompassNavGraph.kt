// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mubarak.mbcompass.ui.compass.CompassApp
import com.mubarak.mbcompass.ui.location.UserLocation
import com.mubarak.mbcompass.ui.settings.SettingsScreen

@Composable
fun CompassNavGraph(
    modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()
) {
    NavHost(
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        navController = navController,
        startDestination = Compass
    ) {
        composable<Compass> {
            CompassApp(
                navigateToMap = { navController.navigateWithBackStack(UserLocation) },
                navigateToSettings = { navController.navigateWithBackStack(Settings) })
        }

        composable<UserLocation>(
            enterTransition = {
                fadeThroughEnter()
            }, exitTransition = {
                fadeThroughExit()
            }, popEnterTransition = {
                fadeThroughEnter()
            }, popExitTransition = {
                fadeThroughExit()
            }) {
            UserLocation(navigateUp = { navController.navigateUp() })
        }

        composable<Settings>(
            enterTransition = {
                fadeThroughEnter()
            }, exitTransition = {
                fadeThroughExit()
            }, popEnterTransition = {
                fadeThroughEnter()
            }, popExitTransition = {
                fadeThroughExit()
            }) {
            SettingsScreen(onBack = { navController.navigateUp() })
        }
    }
}

fun NavController.navigateWithBackStack(route: Any) {
    navigate(route) {
        popUpTo(this@navigateWithBackStack.graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}

// `fadeThrough` transition is recommended for screens that aren't related:
// https://m3.material.io/styles/motion/transitions/transition-patterns#f852afd2-396f-49fd-a265-5f6d96680e16

fun fadeThroughEnter(): EnterTransition =
    fadeIn(
        initialAlpha = 0.4f,
        animationSpec = tween(durationMillis = 300)
    )

fun fadeThroughExit(): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis = 250
        )
    )