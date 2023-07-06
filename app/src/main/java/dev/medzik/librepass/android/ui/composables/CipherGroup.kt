package dev.medzik.librepass.android.ui.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    content()
}

@Composable
fun CipherGroup(
    @StringRes name: Int,
    content: @Composable () -> Unit = {}
) = CipherGroup(name = stringResource(name), content = content)

@Composable
fun Group(
    name: String,
    content: @Composable () -> Unit = {}
) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    content()

    Spacer(
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun Group(
    @StringRes name: Int,
    content: @Composable () -> Unit = {}
) = Group(name = stringResource(name), content = content)
