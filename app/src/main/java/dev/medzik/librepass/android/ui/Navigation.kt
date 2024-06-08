package dev.medzik.librepass.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.librepass.android.MainActivity
import dev.medzik.librepass.android.common.NavigationAnimations
import dev.medzik.librepass.android.ui.components.TopBar
import dev.medzik.librepass.android.ui.components.TopBarBackIcon
import dev.medzik.librepass.android.ui.screens.Welcome
import dev.medzik.librepass.android.ui.screens.WelcomeScreen
import dev.medzik.librepass.android.ui.screens.auth.Unlock
import dev.medzik.librepass.android.ui.screens.auth.authNavigation
import dev.medzik.librepass.android.ui.screens.settings.settingsNavigation
import dev.medzik.librepass.android.ui.screens.vault.Vault
import dev.medzik.librepass.android.ui.screens.vault.vaultNavigation

@Composable
fun LibrePassNavigation(viewModel: LibrePassViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Lifecycle events handler.
    // This calls the `onResume` function from MainActivity when the application is resumed.
    // This is used to lock the vault after X minutes of application sleep in memory.
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            // when the application was resumed
            Lifecycle.State.RESUMED -> {
                // calls the `onResume` function from MainActivity
                (context as MainActivity).onResume(navController)
            }
            // ignore any other lifecycle state
            else -> {}
        }
    }

    fun getStartRoute(): Any {
        // if a user is not logged in, show welcome screen
        viewModel.credentialRepository.get() ?: return Welcome

        // if user secrets are not set, show unlock screen
        if (viewModel.vault.aesKey.isEmpty())
            return Unlock

        // else where the user secrets are set, show vault screen
        return Vault
    }

    NavHost(
        navController,
        startDestination = remember { getStartRoute() },
        modifier = Modifier.imePadding(),
        enterTransition = {
            NavigationAnimations.enterTransition()
        },
        exitTransition = {
            NavigationAnimations.exitTransition()
        },
        popEnterTransition = {
            NavigationAnimations.popEnterTransition()
        },
        popExitTransition = {
            NavigationAnimations.popExitTransition()
        }
    ) {
        composable<Welcome> {
            WelcomeScreen(navController)
        }

        authNavigation(navController)

        vaultNavigation(navController)

        settingsNavigation(navController)
    }
}

@Composable
fun DefaultScaffold(
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    horizontalPadding: Boolean = true,
    composable: @Composable () -> Unit
) {
    Scaffold(
        topBar = { topBar() },
        floatingActionButton = { floatingActionButton() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = if (horizontalPadding) 16.dp else 0.dp)
        ) {
            composable()
        }
    }
}

@Composable
fun TopBarWithBack(@StringRes title: Int, navController: NavController) {
    TopBar(
        title = stringResource(title),
        navigationIcon = { TopBarBackIcon(navController) }
    )
}
