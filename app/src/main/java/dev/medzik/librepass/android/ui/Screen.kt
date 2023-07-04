package dev.medzik.librepass.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Argument.CipherId
import dev.medzik.librepass.android.ui.screens.PasswordGenerator
import dev.medzik.librepass.android.ui.screens.WelcomeScreen
import dev.medzik.librepass.android.ui.screens.auth.LoginScreen
import dev.medzik.librepass.android.ui.screens.auth.RegisterScreen
import dev.medzik.librepass.android.ui.screens.auth.UnlockScreen
import dev.medzik.librepass.android.ui.screens.ciphers.CipherAddEditView
import dev.medzik.librepass.android.ui.screens.ciphers.CipherViewScreen
import dev.medzik.librepass.android.ui.screens.dashboard.DashboardNavigation
import dev.medzik.librepass.android.utils.Navigation.getString
import dev.medzik.librepass.android.utils.getUserSecretsSync
import dev.medzik.librepass.types.cipher.Cipher
import java.util.UUID

/**
 * Enum class for navigation between screens.
 */
enum class Argument {
    CipherId;

    /**
     * Get argument key (e.g. "{cipherid}").
     * Used to fill route with argument.
     * @see fill
     */
    val key get() = "{${name.lowercase()}}"

    /**
     * Get argument name (e.g. "cipherid").
     * Used to get argument from [NavController].
     * @see NavController.getString
     */
    val get get() = name.lowercase()
}

/**
 * Enum class for navigation between screens.
 * @property route Route to screen.
 * @property arguments List of arguments to fill route with. If null, route will not be filled.
 * @see Argument
 * @see fill
 */
enum class Screen(private val route: String, private val arguments: List<Argument>? = null) {
    Welcome("welcome"),
    Register("register"),
    Login("login"),
    Unlock("unlock"),
    Dashboard("dashboard"),
    CipherView("cipher-view", listOf(CipherId)),
    CipherAdd("cipher-add"),
    CipherEdit("cipher-edit", listOf(CipherId)),
    PasswordGenerator("password-generator");

    /**
     * Get the route with arguments (e.g. "dashboard/{cipherid}").
     * The arguments must be filled [fill] if you want to change the screen.
     * @see fill
     */
    val get get() =
        if (arguments != null)
            "$route/${arguments.joinToString("/") { it.key }}"
        else route // if no arguments, return route without arguments

    /**
     * Fill route with arguments for navigation controller.
     * @param arguments List of arguments to fill route with.
     * @return Filled route.
     * @throws IllegalArgumentException If number of arguments is not equal to number of arguments in route.
     * @see Argument
     */
    @Throws(IllegalArgumentException::class)
    fun fill(vararg arguments: Pair<Argument, String>): String {
        if (arguments.size != (this.arguments?.size ?: 0))
            throw IllegalArgumentException("Invalid number of arguments. Expected ${this.arguments?.size}, got ${arguments.size}")

        var route = get
        for (argument in arguments)
            route = route.replace(argument.first.key, argument.second)

        return route
    }
}

@Composable
fun LibrePassNavController() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val repository = context.getRepository()
    val userSecrets = context.getUserSecretsSync()

    NavHost(
        navController = navController,
        startDestination = repository.credentials.get()?.let {
            if (userSecrets != null)
                Screen.Dashboard.get
            else
                Screen.Unlock.get
        } ?: Screen.Welcome.get
    ) {
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
            DashboardNavigation(mainNavController = navController)
        }

        composable(Screen.CipherView.get) {
            CipherViewScreen(navController = navController)
        }

        composable(Screen.CipherAdd.get) {
            CipherAddEditView(navController = navController)
        }

        composable(Screen.CipherEdit.get) {
            val cipherId = navController.getString(CipherId)
                ?: return@composable

            val cipherTable = repository.cipher.get(UUID.fromString(cipherId))
                ?: return@composable

            val cipher = Cipher(cipherTable.encryptedCipher, userSecrets!!.secretKey)

            CipherAddEditView(navController = navController, baseCipher = cipher)
        }

        composable(Screen.PasswordGenerator.get) {
            PasswordGenerator(navController = navController)
        }
    }
}
