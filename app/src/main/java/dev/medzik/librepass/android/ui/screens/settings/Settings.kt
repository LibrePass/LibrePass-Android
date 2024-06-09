package dev.medzik.librepass.android.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import dev.medzik.android.components.ui.IconBox
import dev.medzik.android.components.ui.preference.BasicPreference
import dev.medzik.librepass.android.R
import kotlinx.serialization.Serializable

@Serializable
object Settings

@Composable
fun SettingsScreen(navController: NavController) {
    Column {
        BasicPreference(
            title = stringResource(R.string.Settings_Security),
            leading = { IconBox(Icons.Default.Fingerprint) },
            onClick = { navController.navigate(SettingsSecurity) }
        )

        BasicPreference(
            leading = { IconBox(Icons.Default.ManageAccounts) },
            title = stringResource(R.string.Settings_Account),
            onClick = { navController.navigate(SettingsAccount) }
        )
    }
}
