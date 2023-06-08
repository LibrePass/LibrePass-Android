package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.utils.navigation.getString
import dev.medzik.librepass.android.utils.navigation.navigate

enum class DashboardNavigationItem(val route: String, val icon: ImageVector, val titleId: Int) {
    Dashboard("dashboard", Icons.Default.Lock, R.string.DashboardBottomNav_Dashboard),
    Settings("settings", Icons.Default.Settings, R.string.DashboardBottomNav_Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardNavigation(mainNavController: NavController) {
    // get secret key from navController
    val secretKey = mainNavController.getString(Argument.SecretKey)
        ?: return

    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    // bottom sheet
    var openBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sheetContent by remember { mutableStateOf<@Composable () -> Unit>({ Text("") }) } // text because without it animation is not working

    var currentScreenId by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(
                    id = if (currentScreenId == 0) R.string.TopBar_Vault else R.string.TopBar_Settings
                )
            )
        },
        bottomBar = {
            BottomAppBar {
                DashboardBottomNavigationBar(
                    navController = navController,
                    onItemSelected = { currentScreenId = it }
                )
            }
        },
        floatingActionButton = {
            if (currentScreenId == 0) {
                FloatingActionButton(
                    onClick = {
                        mainNavController.navigate(
                            screen = Screen.CipherAdd,
                            argument = Argument.SecretKey to secretKey
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
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
                composable(DashboardNavigationItem.Settings.route) {
                    SettingsScreen(navController = mainNavController)
                }
            }
        }
    }

    // bottom sheet
    if (openBottomSheet) {
        ModalBottomSheet(
            // TODO: I don't know if this is how it's done but it works
            tonalElevation = 3.dp,
            onDismissRequest = { openBottomSheet = false },
            sheetState = sheetState
        ) {
            sheetContent()
        }
    }
}

@Composable
fun DashboardBottomNavigationBar(navController: NavController, onItemSelected: (Int) -> Unit) {
    val items = listOf(
        DashboardNavigationItem.Dashboard,
        DashboardNavigationItem.Settings
    )
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    var currentRoute by rememberSaveable { mutableStateOf(DashboardNavigationItem.Dashboard.route) }

    items.forEachIndexed { index, navigationItem ->
        if (navigationItem.route == currentRoute) {
            selectedItem = index
        }
    }

    // TODO: save selectedItem and currentRoute after context.recreate()

    NavigationBar(
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                alwaysShowLabel = true,
                icon = { Icon(item.icon, contentDescription = stringResource(id = item.titleId)) },
                label = { Text(stringResource(id = item.titleId)) },
                selected = selectedItem == index,
                onClick = {
                    onItemSelected(index)
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
