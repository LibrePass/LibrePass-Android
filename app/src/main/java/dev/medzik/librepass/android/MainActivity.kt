package dev.medzik.librepass.android

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.data.Settings
import dev.medzik.librepass.android.ui.LibrePassNavController
import dev.medzik.librepass.android.ui.theme.LibrePassTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("LibrePass", "Uncaught exception", e)
            finish()
        }

        // This will lay out our app behind the system bars (to make them transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // database repository
        val repository = Repository(this)
        // get settings or use default
        val settings = repository.settings.get()
            ?: Settings()

        // theme settings
        val autoTheme = settings.theme == 0
        val darkTheme = settings.theme == 2

        setContent {
            LibrePassTheme(
                darkTheme = darkTheme || (autoTheme && isSystemInDarkTheme()),
                dynamicColor = settings.dynamicColor
            ) {
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
