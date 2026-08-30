package mx.unam.gacetacu.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.unam.gacetacu.R
import mx.unam.gacetacu.feature.map.CUMapScreen
import mx.unam.gacetacu.feature.news.NewsScreen
import mx.unam.gacetacu.feature.profile.ProfileScreen
import mx.unam.gacetacu.feature.schedule.ScheduleScreen
import mx.unam.gacetacu.feature.settings.SettingsScreen
import mx.unam.gacetacu.feature.transport.TransportScreen

sealed class Dest(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object News : Dest("news", R.string.nav_news, Icons.Default.Newspaper)
    data object Schedule : Dest("schedule", R.string.nav_schedule, Icons.Default.CalendarMonth)
    data object Transport : Dest("transport", R.string.nav_transport, Icons.Default.DirectionsBus)
    data object Map : Dest("map", R.string.nav_map, Icons.Default.Map)
    data object Profile : Dest("profile", R.string.nav_profile, Icons.Default.Person)
    data object Settings : Dest("settings", R.string.nav_settings, Icons.Default.Settings)
}

private val bottomItems = listOf(Dest.News, Dest.Schedule, Dest.Transport, Dest.Map, Dest.Profile)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                bottomItems.forEach { dest ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = stringResource(dest.labelRes)) },
                        label = { Text(stringResource(dest.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Dest.News.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Dest.News.route) { NewsScreen(onOpenSettings = { navController.navigate(Dest.Settings.route) }) }
            composable(Dest.Schedule.route) { ScheduleScreen() }
            composable(Dest.Transport.route) { TransportScreen() }
            composable(Dest.Map.route) { CUMapScreen() }
            composable(Dest.Profile.route) { ProfileScreen() }
            composable(Dest.Settings.route) { SettingsScreen() }
        }
    }
}
