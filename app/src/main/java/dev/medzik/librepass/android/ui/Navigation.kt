package dev.medzik.librepass.android.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.android.components.NavArgument
import dev.medzik.android.components.NavScreen
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberDialogState
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.librepass.android.MainActivity
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.components.CipherTypeDialog
import dev.medzik.librepass.android.ui.components.TopBar
import dev.medzik.librepass.android.ui.components.TopBarBackIcon
import dev.medzik.librepass.android.ui.screens.WelcomeScreen
import dev.medzik.librepass.android.ui.screens.auth.AddCustomServerScreen
import dev.medzik.librepass.android.ui.screens.auth.LoginScreen
import dev.medzik.librepass.android.ui.screens.auth.RegisterScreen
import dev.medzik.librepass.android.ui.screens.auth.UnlockScreen
import dev.medzik.librepass.android.ui.screens.settings.SettingsAccountScreen
import dev.medzik.librepass.android.ui.screens.settings.SettingsAppearanceScreen
import dev.medzik.librepass.android.ui.screens.settings.SettingsScreen
import dev.medzik.librepass.android.ui.screens.settings.SettingsSecurityScreen
import dev.medzik.librepass.android.ui.screens.settings.account.SettingsAccountChangeEmailScreen
import dev.medzik.librepass.android.ui.screens.settings.account.SettingsAccountChangePasswordScreen
import dev.medzik.librepass.android.ui.screens.settings.account.SettingsAccountDeleteAccountScreen
import dev.medzik.librepass.android.ui.screens.vault.CipherAddScreen
import dev.medzik.librepass.android.ui.screens.vault.CipherEditScreen
import dev.medzik.librepass.android.ui.screens.vault.CipherViewScreen
import dev.medzik.librepass.android.ui.screens.vault.OtpConfigure
import dev.medzik.librepass.android.ui.screens.vault.PasswordGeneratorScreen
import dev.medzik.librepass.android.ui.screens.vault.SearchScreen
import dev.medzik.librepass.android.ui.screens.vault.VaultScreen

enum class Argument : NavArgument {
    CipherId,
    CipherType
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
                stringResource(R.string.Register),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { RegisterScreen(it) }
    ),
    Login(
        topBar = {
            TopBar(
                stringResource(R.string.Login),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { LoginScreen(it) }
    ),
    AddCustomServer(
        topBar = {
            TopBar(
                stringResource(R.string.AddServer),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { AddCustomServerScreen(it) }
    ),

    // Vault
    Unlock(
        topBar = { TopBar(stringResource(R.string.Unlock)) },
        composable = { UnlockScreen(it) }
    ),
    Vault(
        topBar = {
            TopBar(
                stringResource(R.string.Vault),
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
                            text = { Text(stringResource(R.string.Settings)) },
                            onClick = {
                                expanded = false
                                it.navigate(Settings)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.LockVault)) },
                            onClick = {
                                (context as MainActivity).vault.deleteSecrets(context)

                                // close application
                                (context as Activity).finish()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = { navController ->
            val dialogState = rememberDialogState()

            FloatingActionButton(
                onClick = { dialogState.show() }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }

            CipherTypeDialog(
                dialogState,
                onSelected = { cipherType ->
                    navController.navigate(
                        CipherAdd,
                        args =
                            arrayOf(
                                Argument.CipherType to cipherType.ordinal.toString()
                            )
                    )
                }
            )
        },
        composable = { VaultScreen(it) }
    ),
    CipherView(
        args = arrayOf(Argument.CipherId),
        customScaffold = true,
        composable = { CipherViewScreen(it) }
    ),
    CipherAdd(
        args = arrayOf(Argument.CipherType),
        customScaffold = true,
        composable = { CipherAddScreen(it) }
    ),
    CipherEdit(
        args = arrayOf(Argument.CipherId),
        customScaffold = true,
        composable = { CipherEditScreen(it) }
    ),
    ConfigureOtp(
        args = arrayOf(Argument.CipherId),
        customScaffold = true,
        composable = { OtpConfigure(it) }
    ),
    PasswordGenerator(
        topBar = {
            TopBar(
                stringResource(R.string.PasswordGenerator),
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
                stringResource(R.string.Settings),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsScreen(it) },
        noHorizontalPadding = true
    ),
    SettingsAppearance(
        topBar = {
            TopBar(
                stringResource(R.string.Settings_Appearance),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsAppearanceScreen() },
        noHorizontalPadding = true
    ),
    SettingsSecurity(
        topBar = {
            TopBar(
                stringResource(R.string.Settings_Security),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsSecurityScreen() },
        noHorizontalPadding = true
    ),
    SettingsAccount(
        topBar = {
            TopBar(
                stringResource(R.string.Settings_Account),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsAccountScreen(it) },
        noHorizontalPadding = true
    ),
    SettingsAccountChangeEmail(
        topBar = {
            TopBar(
                stringResource(R.string.ChangeEmail),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsAccountChangeEmailScreen(it) }
    ),
    SettingsAccountChangePassword(
        topBar = {
            TopBar(
                stringResource(R.string.ChangePassword),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsAccountChangePasswordScreen(it) }
    ),
    SettingsAccountDeleteAccount(
        topBar = {
            TopBar(
                stringResource(R.string.DeleteAccount),
                navigationIcon = { TopBarBackIcon(it) }
            )
        },
        composable = { SettingsAccountDeleteAccountScreen(it) }
    ),
}

@Composable
fun LibrePassNavigation(viewModel: LibrePassViewModel = hiltViewModel()) {
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

    fun getStartRoute(): String {
        // if a user is not logged in, show welcome screen
        viewModel.credentialRepository.get() ?: return Screen.Welcome.getRoute()

        // if user secrets are not set, show unlock screen
        if (viewModel.vault.aesKey.isEmpty())
            return Screen.Unlock.getRoute()

        // else where the user secrets are set, show vault screen
        return Screen.Vault.getRoute()
    }

    NavHost(
        navController,
        startDestination = remember { getStartRoute() },
        modifier = Modifier.imePadding()
    ) {
        for (screen in Screen.entries) {
            composable(
                route = screen.getRoute(),
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(500)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(500)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(500)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(500)
                    )
                }
            ) {
                if (screen.customScaffold) {
                    screen.composable(navController)
                } else {
                    Scaffold(
                        topBar = { screen.topBar(navController) },
                        floatingActionButton = { screen.floatingActionButton(navController) }
                    ) { innerPadding ->
                        Column(
                            modifier =
                                Modifier
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
