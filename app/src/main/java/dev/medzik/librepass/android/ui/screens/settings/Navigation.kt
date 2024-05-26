package dev.medzik.librepass.android.ui.screens.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.DefaultScaffold
import dev.medzik.librepass.android.ui.TopBarWithBack
import dev.medzik.librepass.android.ui.screens.settings.account.*

fun NavGraphBuilder.settingsNavigation(navController: NavController) {
    composable<Settings> {
        DefaultScaffold(
            topBar = { TopBarWithBack(title = R.string.Settings, navController) },
            horizontalPadding = false
        ) {
            SettingsScreen(navController)
        }
    }

    composable<SettingsAppearance> {
        DefaultScaffold(
            topBar = { TopBarWithBack(title = R.string.Settings_Appearance, navController) },
            horizontalPadding = false
        ) {
            SettingsAppearanceScreen()
        }
    }

    composable<SettingsSecurity> {
        DefaultScaffold(
            topBar = { TopBarWithBack(title = R.string.Settings_Security, navController) },
            horizontalPadding = false
        ) {
            SettingsSecurityScreen()
        }
    }

    composable<SettingsAccount> {
        DefaultScaffold(
            topBar = { TopBarWithBack(title = R.string.Settings_Account, navController) },
            horizontalPadding = false
        ) {
            SettingsAccountScreen(navController)
        }
    }

    composable<SettingsAccountChangeEmail> {
        DefaultScaffold(
            topBar = { TopBarWithBack(title = R.string.ChangeEmail, navController) }
        ) {
            SettingsAccountChangeEmailScreen(navController)
        }
    }

    composable<SettingsAccountChangePassword> {
        DefaultScaffold(
            topBar = { TopBarWithBack(title = R.string.ChangePassword, navController) }
        ) {
            SettingsAccountChangePasswordScreen(navController)
        }
    }

    composable<SettingsAccountDeleteAccount> {
        DefaultScaffold(
            topBar = { TopBarWithBack(title = R.string.DeleteAccount, navController) }
        ) {
            SettingsAccountDeleteAccountScreen(navController)
        }
    }
}
