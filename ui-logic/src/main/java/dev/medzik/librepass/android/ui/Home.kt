package dev.medzik.librepass.android.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.librepass.android.ui.vault.VaultHome
import dev.medzik.librepass.android.ui.vault.VaultHomeScreen
import kotlinx.serialization.Serializable

@Serializable
object Home

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val credentials = viewModel.getCredentials()

    if (credentials != null) {
        val args = VaultHome(
            credentials = credentials
        )

        VaultHomeScreen(args, navController)
    } else {
        WelcomeScreen(navController)
    }
}
