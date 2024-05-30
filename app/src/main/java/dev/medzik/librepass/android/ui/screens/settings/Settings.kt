package dev.medzik.librepass.android.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import dev.medzik.android.components.PreferenceEntry
import dev.medzik.librepass.android.R
import kotlinx.serialization.Serializable

@Serializable
object Settings

@Composable
fun SettingsScreen(navController: NavController) {
    Column {
        PreferenceEntry(
            icon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
            title = stringResource(R.string.Settings_Security),
            onClick = { navController.navigate(SettingsSecurity) }
        )

        PreferenceEntry(
            icon = { Icon(Icons.Default.ManageAccounts, contentDescription = null) },
            title = stringResource(R.string.Settings_Account),
            onClick = { navController.navigate(SettingsAccount) }
        )
    }
}
