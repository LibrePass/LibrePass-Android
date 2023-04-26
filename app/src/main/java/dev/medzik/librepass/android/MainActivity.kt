package dev.medzik.librepass.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.screens.CipherAddEditView
import dev.medzik.librepass.android.ui.screens.CipherViewScreen
import dev.medzik.librepass.android.ui.screens.DashboardScreen
import dev.medzik.librepass.android.ui.screens.LoginScreen
import dev.medzik.librepass.android.ui.screens.RegisterScreen
import dev.medzik.librepass.android.ui.screens.UnlockScreen
import dev.medzik.librepass.android.ui.screens.WelcomeScreen
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LibrePassTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LibrePassApp()
                }
            }
        }
    }
}

@Composable
fun LibrePassApp() {
    val navController = rememberNavController()

    val repository = Repository(context = LocalContext.current)

    NavHost(navController = navController, startDestination = repository.credentials.get()?.let { Screen.Unlock.get } ?: Screen.Welcome.get) {
        composable(Screen.Welcome.get) {
            WelcomeScreen(navController = navController)
        }

        composable(Screen.Register.get) {
            RegisterScreen(navController = navController)
        }

        composable(Screen.Login.get) {
            LoginScreen(navController = navController)
        }

        composable(Screen.Unlock.get) {
            UnlockScreen(navController = navController)
        }

        composable(Screen.Dashboard.get) {
            DashboardScreen(navController = navController)
        }

        composable(Screen.CipherView.get) {
            CipherViewScreen(navController = navController)
        }

        composable(Screen.CipherAdd.get) {
            CipherAddEditView(navController = navController)
        }

        composable(Screen.CipherEdit.get) {
            val cipherId = navController.previousBackStackEntry?.arguments?.getString(Argument.CipherId.get)
                ?: return@composable

            val encryptionKey = navController.previousBackStackEntry?.arguments?.getString(Argument.EncryptionKey.get)
                ?: return@composable

            val cipherTable = repository.cipher.get(UUID.fromString(cipherId)) ?: return@composable

            val cipher = cipherTable.encryptedCipher.toCipher(encryptionKey)

            CipherAddEditView(navController = navController, baseCipher = cipher)
        }
    }
}
