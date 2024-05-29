package dev.medzik.librepass.android.ui.screens.settings.account

import androidx.navigation.NavController
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.screens.Welcome
import dev.medzik.librepass.android.common.popUpToDestination
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
            popUpToDestination(navController)
        }
    }
}
