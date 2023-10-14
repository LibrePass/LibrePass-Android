package dev.medzik.librepass.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.android.components.NavArgument
import dev.medzik.android.components.NavScreen
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.librepass.android.MainActivity
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.screens.PasswordGeneratorScreen
import dev.medzik.librepass.android.ui.screens.WelcomeScreen
import dev.medzik.librepass.android.ui.screens.auth.AddCustomServer
import dev.medzik.librepass.android.ui.screens.auth.LoginScreen
import dev.medzik.librepass.android.ui.screens.auth.RegisterScreen
import dev.medzik.librepass.android.ui.screens.auth.UnlockScreen
import dev.medzik.librepass.android.ui.screens.dashboard.CipherAddScreen
import dev.medzik.librepass.android.ui.screens.dashboard.CipherEditScreen
import dev.medzik.librepass.android.ui.screens.dashboard.CipherViewScreen
import dev.medzik.librepass.android.ui.screens.dashboard.SearchScreen
import dev.medzik.librepass.android.ui.screens.dashboard.VaultScreen
import dev.medzik.librepass.android.ui.screens.settings.SettingsAccountScreen
import dev.medzik.librepass.android.ui.screens.settings.SettingsAppearanceScreen
import dev.medzik.librepass.android.ui.screens.settings.SettingsScreen
import dev.medzik.librepass.android.ui.screens.settings.SettingsSecurityScreen
import dev.medzik.librepass.android.utils.SecretStore
import dev.medzik.librepass.android.utils.SecretStore.getUserSecrets
import dev.medzik.librepass.android.utils.TopBar
import dev.medzik.librepass.android.utils.TopBarBackIcon

enum class Argument : NavArgument {
    CipherId
}

enum class Screen(
    override val args: Array<NavArgument>? = null,
    val customScaffold: Boolean = false,
    val topBar: @Composable (navController: NavController) -> Unit = {},
    val floatingActionButton: @Composable (navController: NavController) -> Unit = {},
    val composable: @Composable (navController: NavController) -> Unit,
    val noHorizontalPadding: Boolean = false
) : NavScreen {
    Welcome(
        customScaffold = true,
        composable = { WelcomeScreen(it) }
    ),

    // Auth
    Register(
        topBar = {
            TopBar(
                stringResource(R.string.TopBar_Register),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { RegisterScreen(it) }
    ),
    Login(
        topBar = {
            TopBar(
                stringResource(R.string.TopBar_Login),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { LoginScreen(it) }
    ),
    AddCustomServer(
        topBar = {
            TopBar(
                stringResource(R.string.TopBar_AddCustomServer),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { AddCustomServer(it) }
    ),

    // Vault
    Unlock(
        topBar = { TopBar(stringResource(R.string.TopBar_Unlock)) },
        composable = { UnlockScreen(it) }
    ),
    Vault(
        topBar = {
            TopBar(
                stringResource(R.string.TopBar_Vault),
                actions = {
                    val context = LocalContext.current
                    var expanded by rememberMutableBoolean()
                    IconButton(onClick = { it.navigate(Search) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.VaultDropdownMenuNav_Settings)) },
                            onClick = {
                                expanded = false
                                it.navigate(Settings)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.VaultDropdownMenuNav_Lock)) },
                            onClick = {
                                // delete secrets
                                SecretStore.delete(context)

                                // close application
                                (context as android.app.Activity).finish()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { it.navigate(CipherAdd) }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        composable = { VaultScreen(it) }
    ),
    CipherView(
        args = arrayOf(Argument.CipherId),
        customScaffold = true,
        composable = { CipherViewScreen(it) }
    ),
    CipherAdd(
        customScaffold = true,
        composable = { CipherAddScreen(it) }
    ),
    CipherEdit(
        args = arrayOf(Argument.CipherId),
        customScaffold = true,
        composable = { CipherEditScreen(it) }
    ),
    PasswordGenerator(
        topBar = {
            TopBar(
                stringResource(R.string.TopBar_PasswordGenerator),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { PasswordGeneratorScreen(it) }
    ),
    Search(
        customScaffold = true,
        composable = { SearchScreen(it) }
    ),

    // Settings
    Settings(
        topBar = {
            TopBar(
                stringResource(R.string.TopBar_Settings),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsScreen(it) },
        noHorizontalPadding = true
    ),
    SettingsAppearance(
        topBar = {
            TopBar(
                stringResource(R.string.Settings_Group_Appearance),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsAppearanceScreen() },
        noHorizontalPadding = true
    ),
    SettingsSecurity(
        topBar = {
            TopBar(
                stringResource(R.string.Settings_Group_Security),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsSecurityScreen() },
        noHorizontalPadding = true
    ),
    SettingsAccount(
        topBar = {
            TopBar(
                stringResource(R.string.Settings_Group_Account),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsAccountScreen(it) },
        noHorizontalPadding = true
    )
}

@Composable
fun LibrePassNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Lifecycle events handler.
    // This calls the `onResume` function from MainActivity when the application is resumed.
    // This is used to lock the vault after X minutes of application sleep in memory.
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            // when the application was resumed
            Lifecycle.State.RESUMED -> {
                // calls the `onResume` function from MainActivity
                (context as MainActivity).onResume(navController)
            }
            // ignore any other lifecycle state
            else -> {}
        }
    }

    val repository = context.getRepository()
    val userSecrets = context.getUserSecrets()

    fun getStartRoute(): String {
        // if a user is not logged in, show welcome screen
        repository.credentials.get() ?: return Screen.Welcome.getRoute()

        // if user secrets are not set, show unlock screen
        userSecrets ?: return Screen.Unlock.getRoute()

        // else where the user secrets are set, show vault screen
        return Screen.Vault.getRoute()
    }

    NavHost(
        navController,
        startDestination = getStartRoute(),
    ) {
        for (screen in Screen.values()) {
            composable(screen.getRoute()) {
                if (screen.customScaffold) {
                    screen.composable(navController)
                } else {
                    Scaffold(
                        topBar = { screen.topBar(navController) },
                        floatingActionButton = { screen.floatingActionButton(navController) },
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = if (screen.noHorizontalPadding) 0.dp else 16.dp)
                        ) {
                            screen.composable(navController)
                        }
                    }
                }
            }
        }
    }
}
