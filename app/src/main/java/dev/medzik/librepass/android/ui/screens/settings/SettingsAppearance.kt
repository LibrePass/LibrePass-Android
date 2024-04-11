package dev.medzik.librepass.android.ui.screens.settings

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

@Composable
fun SettingsAppearanceScreen() {
    val context = LocalContext.current

    val dynamicColor = context.readKey(StoreKey.DynamicColor)

    @Composable
    fun getThemeTranslation(theme: ThemeValues): String {
        return stringResource(
            when (theme) {
                ThemeValues.SYSTEM -> R.string.Theme_SystemDefault
                ThemeValues.LIGHT -> R.string.Theme_Light
                ThemeValues.DARK -> R.string.Theme_Dark
                ThemeValues.BLACK -> R.string.Theme_Black
            }
        )
    }

    val theme = context.readKey(StoreKey.Theme)
    val themeDialogState = rememberDialogState()

    PropertyPreference(
        title = stringResource(R.string.Theme),
        icon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
        currentValue = getThemeTranslation(ThemeValues.entries[theme]),
        onClick = { themeDialogState.show() },
    )

    PickerDialog(
        state = themeDialogState,
        title = stringResource(R.string.Theme),
        items = ThemeValues.entries.toList(),
        onSelected = {
            context.writeKey(StoreKey.Theme, it.ordinal)

            // restart application to apply changes
            ProcessPhoenix.triggerRebirth(context)
        },
    ) {
        Row(
            modifier =
                Modifier
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
                text = getThemeTranslation(it),
                modifier = Modifier
                    .padding(start = 12.dp)
                    .fillMaxWidth()
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
