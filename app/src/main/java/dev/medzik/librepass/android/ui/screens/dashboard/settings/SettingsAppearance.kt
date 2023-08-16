package dev.medzik.librepass.android.ui.screens.dashboard.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.InvertColors
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jakewharton.processphoenix.ProcessPhoenix
import dev.medzik.android.composables.TopBar
import dev.medzik.android.composables.TopBarBackIcon
import dev.medzik.android.composables.dialog.PickerDialog
import dev.medzik.android.composables.dialog.rememberDialogState
import dev.medzik.android.composables.settings.SettingsProperty
import dev.medzik.android.composables.settings.SettingsSwitcher
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.SecretStore.writeKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.android.utils.ThemeValues

@Composable
fun SettingsAppearance(navController: NavController) {
    val context = LocalContext.current

    val dynamicColor = context.readKey(StoreKey.DynamicColor)

    Scaffold(
        topBar = {
            TopBar(
                title = R.string.Settings_Group_Appearance,
                navigationIcon = { TopBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            @Composable
            fun getThemeTranslation(theme: Int): String {
                val themeRes = when (theme) {
                    ThemeValues.SYSTEM.ordinal -> R.string.Settings_SystemDefault
                    ThemeValues.LIGHT.ordinal -> R.string.Settings_Light
                    ThemeValues.DARK.ordinal -> R.string.Settings_Dark
                    // never happens
                    else -> throw UnsupportedOperationException()
                }

                return stringResource(themeRes)
            }

            val theme = context.readKey(StoreKey.Theme)
            val themeDialogState = rememberDialogState()

            SettingsProperty(
                icon = Icons.Default.DarkMode,
                resId = R.string.Settings_Theme,
                currentValue = getThemeTranslation(theme),
                onClick = { themeDialogState.show() },
            )

            PickerDialog(
                state = themeDialogState,
                title = R.string.Settings_Theme,
                items = listOf(0, 1, 2),
                onSelected = {
                    context.writeKey(StoreKey.Theme, it)

                    // restart application to apply changes
                    ProcessPhoenix.triggerRebirth(context)
                },
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        when (it) {
                            ThemeValues.SYSTEM.ordinal -> Icons.Outlined.InvertColors
                            ThemeValues.LIGHT.ordinal -> Icons.Outlined.LightMode
                            ThemeValues.DARK.ordinal -> Icons.Outlined.DarkMode
                            // never happens
                            else -> throw UnsupportedOperationException()
                        },
                        contentDescription = null
                    )

                    Text(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .fillMaxWidth(),
                        text = getThemeTranslation(it)
                    )
                }
            }

            SettingsSwitcher(
                icon = Icons.Default.ColorLens,
                resId = R.string.Settings_MaterialYou,
                checked = dynamicColor,
                onCheckedChange = {
                    context.writeKey(StoreKey.DynamicColor, it)

                    // restart application to apply changes
                    ProcessPhoenix.triggerRebirth(context)
                }
            )
        }
    }
}
