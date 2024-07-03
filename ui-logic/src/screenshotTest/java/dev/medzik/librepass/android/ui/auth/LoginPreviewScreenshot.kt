package dev.medzik.librepass.android.ui.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class LoginPreviewScreenshot {
    @Preview
    @Composable
    fun LightPreview() {
        MaterialTheme {
            LoginScreenPreview()
        }
    }

    @Preview
    @Composable
    fun DarkPreview() {
        MaterialTheme(
            colorScheme = darkColorScheme()
        ) {
            LoginScreenPreview()
        }
    }
}
