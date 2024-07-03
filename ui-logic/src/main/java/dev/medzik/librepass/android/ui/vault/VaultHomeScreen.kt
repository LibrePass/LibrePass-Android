package dev.medzik.librepass.android.ui.vault

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import dev.medzik.librepass.android.database.Credentials

data class VaultHome(
    val credentials: Credentials
)

@Composable
fun VaultHomeScreen(
    args: VaultHome,
    navController: NavController
) {

}
