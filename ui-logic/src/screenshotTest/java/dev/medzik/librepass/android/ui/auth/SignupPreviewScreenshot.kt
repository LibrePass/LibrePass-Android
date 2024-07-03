package dev.medzik.librepass.android.ui.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class SignupPreviewScreenshot {
    @Preview
    @Composable
    fun LightPreview() {
        MaterialTheme {
            SignupPreview()
        }
    }

    @Preview
    @Composable
    fun DarkPreview() {
        MaterialTheme(
            colorScheme = darkColorScheme()
        ) {
            SignupPreview()
        }
    }
}
