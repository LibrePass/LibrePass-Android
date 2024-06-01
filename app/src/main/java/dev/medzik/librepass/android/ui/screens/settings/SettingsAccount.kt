package dev.medzik.librepass.android.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import dev.medzik.android.components.ui.PreferenceEntry
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.screens.Welcome
import dev.medzik.librepass.android.ui.screens.settings.account.SettingsAccountChangeEmail
import dev.medzik.librepass.android.ui.screens.settings.account.SettingsAccountChangePassword
import dev.medzik.librepass.android.ui.screens.settings.account.SettingsAccountDeleteAccount
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
object SettingsAccount

@Composable
fun SettingsAccountScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    PreferenceEntry(
        title = stringResource(R.string.ChangeEmail),
        icon = { Icon(Icons.Default.Email, contentDescription = null) },
        onClick = { navController.navigate(SettingsAccountChangeEmail) }
    )

    PreferenceEntry(
        title = stringResource(R.string.ChangePassword),
        icon = { Icon(Icons.Default.LockReset, contentDescription = null) },
        onClick = { navController.navigate(SettingsAccountChangePassword) }
    )

    PreferenceEntry(
        title = stringResource(R.string.Logout),
        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
        onClick = {
            runBlocking {
                val credentials = viewModel.credentialRepository.get()!!

                viewModel.credentialRepository.drop()
                viewModel.cipherRepository.drop(credentials.userId)

                navController.navigate(
                    Welcome
                ) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = false
                        inclusive = true
                    }
                }
            }
        }
    )

    PreferenceEntry(
        title = stringResource(R.string.DeleteAccount),
        icon = { Icon(Icons.Default.NoAccounts, contentDescription = null) },
        onClick = { navController.navigate(SettingsAccountDeleteAccount) }
    )
}
