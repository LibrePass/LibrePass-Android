package dev.medzik.librepass.android.ui.screens.settings.account

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.screens.Welcome
import kotlinx.coroutines.runBlocking
import java.util.*

fun navigateToWelcomeAndLogout(
    viewModel: LibrePassViewModel,
    navController: NavController,
    userId: UUID
) {
    runBlocking {
        viewModel.credentialRepository.drop()
        viewModel.cipherRepository.drop(userId)
    }

    runOnUiThread {
        navController.navigate(
            Welcome
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = false
                inclusive = true
            }
        }
    }
}
