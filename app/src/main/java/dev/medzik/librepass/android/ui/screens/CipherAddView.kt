package dev.medzik.librepass.android.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.composable.common.TopBar

@Composable
fun CipherAddView(
    navController: NavController
) {
    // get encryption key from navController
    val encryptionKey = navController.currentBackStackEntry?.arguments?.getString(Argument.EncryptionKey.get)
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
