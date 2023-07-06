package dev.medzik.android.composables.res

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Text as MaterialText

@Composable
fun Text(
    @StringRes text: Int,
    modifier: Modifier = Modifier
) {
    MaterialText(
        text = stringResource(text),
        modifier = modifier
    )
}
