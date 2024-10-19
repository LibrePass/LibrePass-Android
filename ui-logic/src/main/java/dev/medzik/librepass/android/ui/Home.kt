package dev.medzik.librepass.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import dev.medzik.librepass.android.database.injection.DatabaseProvider
import dev.medzik.librepass.android.ui.vault.VaultHome
import dev.medzik.librepass.android.ui.vault.VaultHomeScreen
import kotlinx.serialization.Serializable

@Serializable
object Home

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = DatabaseProvider.providesRepository(context)
    val credentials = repository.credentials.get()

    if (credentials != null) {
        val args = VaultHome(
            credentials = credentials
        )

        VaultHomeScreen(args, navController)
    } else {
        WelcomeScreen(navController)
    }
}
