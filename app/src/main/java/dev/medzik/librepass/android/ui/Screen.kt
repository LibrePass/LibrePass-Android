package dev.medzik.librepass.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.screens.PasswordGenerator
import dev.medzik.librepass.android.ui.screens.WelcomeScreen
import dev.medzik.librepass.android.ui.screens.auth.LoginScreen
import dev.medzik.librepass.android.ui.screens.auth.RegisterScreen
import dev.medzik.librepass.android.ui.screens.auth.UnlockScreen
import dev.medzik.librepass.android.ui.screens.ciphers.CipherAddEditView
import dev.medzik.librepass.android.ui.screens.ciphers.CipherViewScreen
import dev.medzik.librepass.android.ui.screens.dashboard.DashboardNavigation
import dev.medzik.librepass.android.utils.DataStore.getUserSecrets
import dev.medzik.librepass.android.utils.Navigation.getString
import dev.medzik.librepass.types.cipher.Cipher
import java.util.UUID

enum class Argument {
    CipherId
}

enum class Screen(private val argument: Argument? = null) {
    Welcome,

    Register,
    Login,

    Unlock,
    Dashboard,
    CipherView(Argument.CipherId),
    CipherAdd,
    CipherEdit(Argument.CipherId),
    PasswordGenerator;

    val route = if (argument != null) "$name/{${argument.name}}" else name

    fun fill(argumentPair: Pair<Argument, String>? = null): String {
        val arg = argumentPair?.first
        val value = argumentPair?.second

        if (arg != argument)
            throw IllegalArgumentException("Invalid arguments. Expected ${this.argument?.name}, got $arg")

        return when (argumentPair) {
            null -> route
            else -> route.replace("{${arg!!.name}}", value!!)
        }
    }
}

@Composable
fun LibrePassNavigation() {
    val context = LocalContext.current

    val navController = rememberNavController()

    val repository = context.getRepository()
    val userSecrets = context.getUserSecrets()

    fun getStartRoute(): String {
        // if a user is not logged in, show welcome screen
        repository.credentials.get()
            ?: return Screen.Welcome.route

        // if user secrets are not set, show unlock screen
        userSecrets
            ?: return Screen.Unlock.route

        // else where the user secrets are set, show dashboard screen
        return Screen.Dashboard.route
    }

    NavHost(
        navController = navController,
        startDestination = getStartRoute()
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.Unlock.route) {
            UnlockScreen(navController)
        }

        composable(Screen.Dashboard.route) {
            DashboardNavigation(mainNavController = navController)
        }

        composable(Screen.CipherView.route) {
            CipherViewScreen(navController = navController)
        }

        composable(Screen.CipherAdd.route) {
            CipherAddEditView(navController = navController)
        }

        composable(Screen.CipherEdit.route) {
            val cipherId = navController.getString(Argument.CipherId)
                ?: return@composable

            val cipherTable = repository.cipher.get(UUID.fromString(cipherId))
                ?: return@composable

            val cipher = Cipher(cipherTable.encryptedCipher, userSecrets!!.secretKey)

            CipherAddEditView(navController = navController, baseCipher = cipher)
        }

        composable(Screen.PasswordGenerator.route) {
            PasswordGenerator(navController)
        }
    }
}
