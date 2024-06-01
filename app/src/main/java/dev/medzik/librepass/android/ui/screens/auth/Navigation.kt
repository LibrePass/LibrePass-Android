package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.DefaultScaffold
import dev.medzik.librepass.android.ui.TopBarWithBack
import dev.medzik.librepass.android.ui.components.TopBar

fun NavGraphBuilder.authNavigation(navController: NavController) {
    composable<Register> {
        DefaultScaffold(
            topBar = { TopBarWithBack(title = R.string.Register, navController) }
        ) {
            RegisterScreen(navController)
        }
    }

    composable<Login> {
        DefaultScaffold(
            topBar = { TopBarWithBack(title = R.string.Login, navController) }
        ) {
            LoginScreen(navController)
        }
    }

    composable<Unlock> {
        DefaultScaffold(
            topBar = { TopBar(title = stringResource(R.string.Unlock)) }
        ) {
            UnlockScreen(navController)
        }
    }

    composable<AddCustomServer> {
        DefaultScaffold(
            topBar = { TopBarWithBack(title = R.string.AddServer, navController) }
        ) {
            AddCustomServerScreen(navController)
        }
    }
}
