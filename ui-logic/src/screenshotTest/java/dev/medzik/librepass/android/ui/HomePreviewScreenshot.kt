package dev.medzik.librepass.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class HomePreviewScreenshot {
    @Preview
    @Composable
    fun HomeScreenLightPreview() {
        MaterialTheme {
            HomeScreenPreview()
        }
    }

    @Preview
    @Composable
    fun HomeScreenDarkPreview() {
        MaterialTheme(
            colorScheme = darkColorScheme()
        ) {
            HomeScreenPreview()
        }
    }
}
