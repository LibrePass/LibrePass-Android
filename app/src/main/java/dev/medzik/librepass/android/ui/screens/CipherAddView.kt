package dev.medzik.librepass.android.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import dev.medzik.librepass.android.ui.composable.TopBar

@Composable
fun CipherAddView(
    navController: NavController
) {
    // get encryption key from navController
    val encryptionKey = navController.currentBackStackEntry?.arguments?.getString("encryptionKey")
        ?: return

    Scaffold(
        topBar = {
            TopBar(
                title = "Add Cipher"
            )
        }
    ) {

    }
}
