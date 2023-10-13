package dev.medzik.librepass.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.android.components.NavArgument
import dev.medzik.android.components.NavScreen
import dev.medzik.android.components.getString
import dev.medzik.librepass.android.MainActivity
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.screens.PasswordGenerator
import dev.medzik.librepass.android.ui.screens.WelcomeScreen
import dev.medzik.librepass.android.ui.screens.auth.AddCustomServer
import dev.medzik.librepass.android.ui.screens.auth.LoginScreen
import dev.medzik.librepass.android.ui.screens.auth.RegisterScreen
import dev.medzik.librepass.android.ui.screens.auth.UnlockScreen
import dev.medzik.librepass.android.ui.screens.ciphers.CipherAddEditView
import dev.medzik.librepass.android.ui.screens.ciphers.CipherViewScreen
import dev.medzik.librepass.android.ui.screens.dashboard.DashboardNavigation
import dev.medzik.librepass.android.ui.screens.dashboard.settings.SettingsAccount
import dev.medzik.librepass.android.ui.screens.dashboard.settings.SettingsAppearance
import dev.medzik.librepass.android.ui.screens.dashboard.settings.SettingsSecurity
import dev.medzik.librepass.android.utils.SecretStore.getUserSecrets
import dev.medzik.librepass.types.cipher.Cipher
import java.util.UUID

enum class Argument : NavArgument {
    CipherId
}

enum class Screen(override val args: Array<NavArgument>? = null) : NavScreen {
    Welcome,

    Register,
    Login,
    AddCustomServer,

    Unlock,
    Dashboard,
    CipherView(arrayOf(Argument.CipherId)),
    CipherAdd,
    CipherEdit(arrayOf(Argument.CipherId)),
    PasswordGenerator,

    SettingsAppearance,
    SettingsSecurity,
    SettingsAccount
}

@Composable
fun LibrePassNavigation() {
    val context = LocalContext.current

    val navController = rememberNavController()

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                (context as MainActivity).onResume(navController)
            }

            else -> {}
        }
    }

    val repository = context.getRepository()
    val userSecrets = context.getUserSecrets()

    fun getStartRoute(): String {
        // if a user is not logged in, show welcome screen
        repository.credentials.get()
            ?: return Screen.Welcome.getRoute()

        // if user secrets are not set, show unlock screen
        userSecrets
            ?: return Screen.Unlock.getRoute()

        // else where the user secrets are set, show dashboard screen
        return Screen.Dashboard.getRoute()
    }

    NavHost(
        navController = navController,
        startDestination = getStartRoute()
    ) {
        composable(Screen.Welcome.getRoute()) {
            WelcomeScreen(navController)
        }

        composable(Screen.Register.getRoute()) {
            RegisterScreen(navController)
        }

        composable(Screen.Login.getRoute()) {
            LoginScreen(navController)
        }

        composable(Screen.AddCustomServer.getRoute()) {
            AddCustomServer(navController)
        }

        composable(Screen.Unlock.getRoute()) {
            UnlockScreen(navController)
        }

        composable(Screen.Dashboard.getRoute()) {
            DashboardNavigation(mainNavController = navController)
        }

        composable(Screen.CipherView.getRoute()) {
            CipherViewScreen(navController = navController)
        }

        composable(Screen.CipherAdd.getRoute()) {
            CipherAddEditView(navController = navController)
        }

        composable(Screen.CipherEdit.getRoute()) {
            val cipherId = navController.getString(Argument.CipherId)
                ?: return@composable

            val cipherTable = repository.cipher.get(UUID.fromString(cipherId))
                ?: return@composable

            val cipher = Cipher(cipherTable.encryptedCipher, context.getUserSecrets()!!.secretKey)

            CipherAddEditView(navController = navController, baseCipher = cipher)
        }

        composable(Screen.PasswordGenerator.getRoute()) {
            PasswordGenerator(navController)
        }

        composable(Screen.SettingsAppearance.getRoute()) {
            SettingsAppearance(navController)
        }

        composable(Screen.SettingsSecurity.getRoute()) {
            SettingsSecurity(navController)
        }

        composable(Screen.SettingsAccount.getRoute()) {
            SettingsAccount(navController)
        }
    }
}
