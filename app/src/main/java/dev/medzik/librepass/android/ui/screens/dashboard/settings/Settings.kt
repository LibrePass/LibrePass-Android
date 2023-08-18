package dev.medzik.librepass.android.ui.screens.dashboard.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import dev.medzik.android.composables.settings.PreferenceEntry
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.utils.navigation.navigate

@Composable
fun SettingsScreen(navController: NavController) {
    Column {
        PreferenceEntry(
            icon = Icons.Default.ColorLens,
            title = stringResource(R.string.Settings_Group_Appearance),
            onClick = { navController.navigate(Screen.SettingsAppearance) }
        )

        PreferenceEntry(
            icon = Icons.Default.Fingerprint,
            title = stringResource(R.string.Settings_Group_Security),
            onClick = { navController.navigate(Screen.SettingsSecurity) }
        )

        PreferenceEntry(
            icon = Icons.Default.AccountCircle,
            title = stringResource(R.string.Settings_Group_Account),
            onClick = { navController.navigate(Screen.SettingsAccount) }
        )
    }
}
