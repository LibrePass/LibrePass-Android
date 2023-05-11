package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class DashboardNavigationItem(val route: String, val icon: ImageVector, val title: String) {
    Dashboard("dashboard", Icons.Default.Lock, "Dashboard"),
    Settings("settings", Icons.Default.Settings, "Settings"),
    Generator("generator", Icons.Default.Refresh, "Generator")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardNavigation(mainNavController: NavController) {
    val navController = rememberNavController()

    // bottom sheet
    val sheetState = rememberModalBottomSheetState()
    val sheetContent = remember { mutableStateOf<@Composable () -> Unit>({ Text("") }) } // text because without it animation is not working

    Scaffold(
        bottomBar = {
            BottomAppBar {
                DashboardBottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(
                PaddingValues(
                    0.dp,
                    0.dp,
                    0.dp,
                    innerPadding.calculateBottomPadding()
                )
            )
        ) {
            NavHost(navController, startDestination = DashboardNavigationItem.Dashboard.route) {
                composable(DashboardNavigationItem.Dashboard.route) {
                    DashboardScreen(
                        navController = mainNavController,
                        sheetState = sheetState,
                        sheetContent = sheetContent
                    )
                }
                composable(DashboardNavigationItem.Generator.route) {
                    // TODO
                    GeneratorScreen()
                }
                composable(DashboardNavigationItem.Settings.route) {
                    // TODO
                    SettingsScreen()
                }
            }
        }
    }

    // bottom sheet
    BottomSheetScaffold(
        scaffoldState = BottomSheetScaffoldState(
            bottomSheetState = sheetState,
            snackbarHostState = SnackbarHostState()
        ),
        sheetContent = { sheetContent.value() }
    ) {
        // empty content
    }
}

@Composable
fun GeneratorScreen() {
    Text(text = "Generator")
}

@Composable
fun SettingsScreen() {
    Text(text = "Settings")
}

@Composable
fun DashboardBottomNavigationBar(navController: NavController) {
    val items = listOf(
        DashboardNavigationItem.Dashboard,
        DashboardNavigationItem.Generator,
        DashboardNavigationItem.Settings
    )
    var selectedItem by remember { mutableStateOf(0) }
    var currentRoute by remember { mutableStateOf(DashboardNavigationItem.Dashboard.route) }

    items.forEachIndexed { index, navigationItem ->
        if (navigationItem.route == currentRoute) {
            selectedItem = index
        }
    }

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                alwaysShowLabel = true,
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    currentRoute = item.route
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
