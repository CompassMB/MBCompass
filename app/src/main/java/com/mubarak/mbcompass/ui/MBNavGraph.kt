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
import com.mubarak.mbcompass.features.compass.NavScreen
import com.mubarak.mbcompass.features.map.MapScreen
import com.mubarak.mbcompass.features.settings.SettingsScreen
import com.mubarak.mbcompass.features.tracks.TracksScreen
import com.mubarak.mbcompass.navigation.NavigationRoute
import com.mubarak.mbcompass.navigation.SettingsRoute
import com.mubarak.mbcompass.navigation.MapRoute
import com.mubarak.mbcompass.navigation.TracksRoute

@Composable
fun MBNavGraph(
    modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()
) {
    NavHost(
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        navController = navController,
        startDestination = NavigationRoute
    ) {
        composable<NavigationRoute> {
            NavScreen(
                navigateToSettings = { navController.navigateWithBackStack(SettingsRoute) })
        }

        composable<MapRoute>(
            enterTransition = {
                fadeThroughEnter()
            }, exitTransition = {
                fadeThroughExit()
            }, popEnterTransition = {
                fadeThroughEnter()
            }, popExitTransition = {
                fadeThroughExit()
            }) {
            MapScreen()
        }

        composable<TracksRoute>(
            enterTransition = {
                fadeThroughEnter()
            }, exitTransition = {
                fadeThroughExit()
            }, popEnterTransition = {
                fadeThroughEnter()
            }, popExitTransition = {
                fadeThroughExit()
            }) {
            TracksScreen(navController = navController)
        }

        composable<SettingsRoute>(
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