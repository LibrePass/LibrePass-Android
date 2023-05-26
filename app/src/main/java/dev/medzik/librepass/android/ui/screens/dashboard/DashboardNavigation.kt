package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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

    //    Generator("generator", Icons.Default.Refresh, "Generator"),
    Settings("settings", Icons.Default.Settings, "Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardNavigation(mainNavController: NavController) {
    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    // bottom sheet
    var openBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sheetContent by remember { mutableStateOf<@Composable () -> Unit>({ Text("") }) } // text because without it animation is not working

    Scaffold(
        bottomBar = {
            BottomAppBar {
                DashboardBottomNavigationBar(navController = navController)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        openBottomSheet = {
                            sheetContent = it
                            openBottomSheet = true
                        },
                        closeBottomSheet = { openBottomSheet = false },
                        snackbarHostState = snackbarHostState
                    )
                }
//                composable(DashboardNavigationItem.Generator.route) {
//                    // TODO
//                    GeneratorScreen()
//                }
                composable(DashboardNavigationItem.Settings.route) {
                    SettingsScreen(navController = mainNavController)
                }
            }
        }
    }

    // bottom sheet
    if (openBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { openBottomSheet = false },
            sheetState = sheetState
        ) {
            sheetContent()
        }
    }
}

// @Composable
// fun GeneratorScreen() {
//    Text(text = "Generator")
// }

@Composable
fun DashboardBottomNavigationBar(navController: NavController) {
    val items = listOf(
        DashboardNavigationItem.Dashboard,
//        DashboardNavigationItem.Generator,
        DashboardNavigationItem.Settings
    )
    var selectedItem by remember { mutableStateOf(0) }
    var currentRoute by remember { mutableStateOf(DashboardNavigationItem.Dashboard.route) }

    items.forEachIndexed { index, navigationItem ->
        if (navigationItem.route == currentRoute) {
            selectedItem = index
        }
    }

    NavigationBar(
        tonalElevation = 0.dp
    ) {
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
