// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui

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
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Compass
    ) {
        composable<Compass> {
            CompassApp(
                navigateToMap = { navController.navigateWithBackStack(UserLocation) },
                navigateToSettings = { navController.navigateWithBackStack(Settings) })
        }

        composable<UserLocation> {
            UserLocation(navigateUp = { navController.navigateUp() })
        }

        composable<Settings> {
            SettingsScreen(onLicensesClicked = {}, onBack = { navController.navigateUp() })
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