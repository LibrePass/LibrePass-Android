package dev.medzik.librepass.android.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import dev.medzik.android.components.ui.IconBox
import dev.medzik.android.components.ui.preference.BasicPreference
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
    BasicPreference(
        title = stringResource(R.string.ChangeEmail),
        leading = { IconBox(Icons.Default.Email) },
        onClick = { navController.navigate(SettingsAccountChangeEmail) }
    )

    BasicPreference(
        title = stringResource(R.string.ChangePassword),
        leading = { IconBox(Icons.Default.LockReset) },
        onClick = { navController.navigate(SettingsAccountChangePassword) }
    )

    BasicPreference(
        title = stringResource(R.string.Logout),
        leading = { IconBox(Icons.AutoMirrored.Filled.Logout) },
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

    BasicPreference(
        title = stringResource(R.string.DeleteAccount),
        leading = { IconBox(Icons.Default.NoAccounts) },
        onClick = { navController.navigate(SettingsAccountDeleteAccount) }
    )
}
