package dev.medzik.android.composables

import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoadingButton(
    loading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
    ) {
        if (loading)
            LoadingIndicator(animating = true)
        else content()
    }
}
