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
import dev.medzik.librepass.android.ui.LibrePassNavigation
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.android.utils.DataStore.readKeyFromDataStore
import dev.medzik.librepass.android.utils.DataStoreKey
import dev.medzik.librepass.android.utils.DataStoreUserSecrets
import dev.medzik.librepass.android.utils.ThemeValues
import kotlinx.coroutines.runBlocking

lateinit var UserSecretsStore: DataStoreUserSecrets

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("LibrePass", "Uncaught exception", e)
            finish()
        }

        // this will lay out our app behind the system bars (to make them transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val context = this

        // init datastore
        runBlocking { UserSecretsStore = DataStoreUserSecrets.init(context) }

        // get app theme settings
        val dynamicColor = runBlocking { context.readKeyFromDataStore(DataStoreKey.DynamicColor) }
        val theme = runBlocking { context.readKeyFromDataStore(DataStoreKey.Theme) }
        val autoTheme = theme == ThemeValues.SYSTEM.ordinal
        val darkTheme = theme == ThemeValues.DARK.ordinal

        setContent {
            LibrePassTheme(
                darkTheme = darkTheme || (autoTheme && isSystemInDarkTheme()),
                dynamicColor = dynamicColor
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LibrePassNavigation()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        val context = this
        runBlocking { UserSecretsStore.save(context) }
    }
}
