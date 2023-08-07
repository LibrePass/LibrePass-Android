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
import androidx.navigation.NavController
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.LibrePassNavigation
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.android.utils.Navigation.navigate
import dev.medzik.librepass.android.utils.SecretStore
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.android.utils.ThemeValues
import dev.medzik.librepass.android.utils.UserSecrets
import dev.medzik.librepass.android.utils.VaultTimeoutValues

lateinit var UserSecretsStore: UserSecrets

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

        // init datastore
        UserSecretsStore = SecretStore.initialize(this)

        // get app theme settings
        val dynamicColor = this.readKey(StoreKey.DynamicColor)
        val theme = this.readKey(StoreKey.Theme)
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

    override fun onPause() {
        super.onPause()

        // check if user is logged
        val repository = this.getRepository()
        if (repository.credentials.get() == null) return

        val vaultTimeout = this.readKey(StoreKey.VaultTimeout)
        if (vaultTimeout == VaultTimeoutValues.INSTANT.seconds) {
            SecretStore.delete(this)
        } else {
            SecretStore.save(this, UserSecretsStore)
        }
    }

    /**
     * @see [LibrePassNavigation]
     */
    fun onResume(navController: NavController) {
        // check if user is logged
        val repository = this.getRepository()
        if (repository.credentials.get() == null) return

        val vaultTimeout = this.readKey(StoreKey.VaultTimeout)
        val expiresTime = this.readKey(StoreKey.VaultExpiresAt)
        val currentTime = System.currentTimeMillis()

        // check if the vault has expired
        if (vaultTimeout == VaultTimeoutValues.INSTANT.seconds ||
            (vaultTimeout != VaultTimeoutValues.NEVER.seconds && currentTime > expiresTime)
        ) {
            SecretStore.delete(this)

            navController.navigate(Screen.Unlock)
        }
    }
}
