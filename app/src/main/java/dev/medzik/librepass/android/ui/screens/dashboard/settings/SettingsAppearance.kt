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
import dev.medzik.android.components.PickerDialog
import dev.medzik.android.components.PropertyPreference
import dev.medzik.android.components.SwitcherPreference
import dev.medzik.android.components.rememberDialogState
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.SecretStore.writeKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.android.utils.ThemeValues
import dev.medzik.librepass.android.utils.TopBar
import dev.medzik.librepass.android.utils.TopBarBackIcon

@Composable
fun SettingsAppearance(navController: NavController) {
    val context = LocalContext.current

    val dynamicColor = context.readKey(StoreKey.DynamicColor)

    Scaffold(
        topBar = {
            TopBar(
                stringResource(R.string.Settings_Group_Appearance),
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
                    ThemeValues.BLACK.ordinal -> R.string.Settings_Black
                    // never happens
                    else -> throw UnsupportedOperationException()
                }

                return stringResource(themeRes)
            }

            val theme = context.readKey(StoreKey.Theme)
            val themeDialogState = rememberDialogState()

            PropertyPreference(
                title = stringResource(R.string.Settings_Theme),
                icon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                currentValue = getThemeTranslation(theme),
                onClick = { themeDialogState.show() },
            )

            PickerDialog(
                state = themeDialogState,
                title = stringResource(R.string.Settings_Theme),
                items = ThemeValues.entries.toList(),
                onSelected = {
                    context.writeKey(StoreKey.Theme, it.ordinal)

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
                            ThemeValues.SYSTEM -> Icons.Outlined.InvertColors
                            ThemeValues.LIGHT -> Icons.Outlined.LightMode
                            ThemeValues.DARK -> Icons.Outlined.DarkMode
                            ThemeValues.BLACK -> Icons.Outlined.DarkMode
                        },
                        contentDescription = null
                    )

                    Text(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .fillMaxWidth(),
                        text = getThemeTranslation(it.ordinal)
                    )
                }
            }

            SwitcherPreference(
                title = stringResource(R.string.Settings_MaterialYou),
                icon = { Icon(Icons.Default.ColorLens, contentDescription = null) },
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
