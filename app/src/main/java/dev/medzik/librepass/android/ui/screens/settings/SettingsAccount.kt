package dev.medzik.librepass.android.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.components.PreferenceEntry
import dev.medzik.android.components.navigate
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.Screen
import kotlinx.coroutines.runBlocking

@Composable
fun SettingsAccountScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    PreferenceEntry(
        title = stringResource(R.string.ChangePassword),
        icon = { Icon(Icons.Default.LockReset, contentDescription = null) },
        onClick = { navController.navigate(Screen.SettingsAccountChangePassword) }
    )

    PreferenceEntry(
        title = stringResource(R.string.Logout),
        icon = { Icon(Icons.Default.Logout, contentDescription = null) },
        onClick = {
            runBlocking {
                val credentials = viewModel.credentialRepository.get()!!

                viewModel.credentialRepository.drop()
                viewModel.cipherRepository.drop(credentials.userId)

                navController.navigate(
                    screen = Screen.Welcome,
                    disableBack = true
                )
            }
        }
    )

    PreferenceEntry(
        title = stringResource(R.string.DeleteAccount),
        icon = { Icon(Icons.Default.NoAccounts, contentDescription = null) },
        onClick = { navController.navigate(Screen.SettingsAccountDeleteAccount) }
    )
}
