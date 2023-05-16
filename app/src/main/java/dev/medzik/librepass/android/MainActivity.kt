package dev.medzik.librepass.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import dev.medzik.librepass.android.ui.LibrePassNavController
import dev.medzik.librepass.android.ui.theme.LibrePassTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This will lay out our app behind the system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LibrePassTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LibrePassNavController()
                }
            }
        }
    }
}
