package dev.medzik.android.composables.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SettingsGroup(
    name: String,
    content: @Composable () -> Unit
) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(vertical = 4.dp)
    )

    content()
}

@Composable
fun SettingsGroup(
    @StringRes name: Int,
    content: @Composable () -> Unit
) {
    SettingsGroup(
        name = stringResource(name),
        content = content
    )
}
