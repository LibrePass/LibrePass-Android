package dev.medzik.librepass.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.screens.CipherAddEditView
import dev.medzik.librepass.android.ui.screens.CipherViewScreen
import dev.medzik.librepass.android.ui.screens.DashboardScreen
import dev.medzik.librepass.android.ui.screens.LoginScreen
import dev.medzik.librepass.android.ui.screens.RegisterScreen
import dev.medzik.librepass.android.ui.screens.UnlockScreen
import dev.medzik.librepass.android.ui.screens.WelcomeScreen
import java.util.UUID

enum class Argument {
    EncryptionKey,
    CipherId,

    ;

    val key get() = "{${name.lowercase()}}"
    val get get() = name.lowercase()
}

sealed class Screen(private val route: String, private val arguments: List<String>? = null) {
    object Welcome : Screen("welcome")
    object Register : Screen("register")
    object Login : Screen("login")
    object Unlock : Screen("unlock")
    object Dashboard : Screen("dashboard", listOf(Argument.EncryptionKey.key))
    object CipherView : Screen("cipher-view", listOf(Argument.EncryptionKey.key, Argument.CipherId.key))
    object CipherAdd : Screen("cipher-add", listOf(Argument.EncryptionKey.key))
    object CipherEdit : Screen("cipher-edit", listOf(Argument.EncryptionKey.key, Argument.CipherId.key))

    val get get() = if (arguments != null) "$route/${arguments.joinToString("/")}" else route

    @Throws(IllegalArgumentException::class)
    fun fill(vararg arguments: Pair<Argument, String>): String {
        if (arguments.size != this.arguments?.size) {
            println("Invalid number of arguments. Expected ${this.arguments?.size}, got ${arguments.size}")
            throw IllegalArgumentException("Invalid number of arguments. Expected ${this.arguments?.size}, got ${arguments.size}")
        }

        var route = this.get

        for (argument in arguments) {
            route = route.replace(argument.first.key, argument.second)
        }

        return route
    }
}

@Composable
fun LibrePassNavController() {
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
