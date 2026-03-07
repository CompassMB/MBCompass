// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mubarak.mbcompass.navigation.NavigationRoute
import com.mubarak.mbcompass.navigation.TopLevelDestination
import com.mubarak.mbcompass.ui.components.MBCBottomBar

@Composable
fun MBCompassApp(
    navHostController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentRoute: Any = TopLevelDestination
        .firstOrNull { destination ->
            currentDestination?.hierarchy?.any {
                it.hasRoute(destination.route::class)
            } == true
        }?.route ?: NavigationRoute

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            MBCBottomBar(
                destinations = TopLevelDestination,
                currentRoute = currentRoute,
                navigateToRoute = { navHostController.navigateWithBackStack(it) }
            )
        }
    ) { innerPadding ->
        MBCNavGraph(
            modifier = Modifier.padding(innerPadding),
            navController = navHostController
        )
    }
}