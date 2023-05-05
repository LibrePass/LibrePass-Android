package dev.medzik.librepass.android.ui.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CipherGroup(
    name: String,
    content: @Composable () -> Unit = {}
) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
    )

    content()
}
