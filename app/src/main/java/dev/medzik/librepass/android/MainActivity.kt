package dev.medzik.librepass.android

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import dagger.hilt.android.AndroidEntryPoint
import dev.medzik.android.utils.openEmailApplication
import dev.medzik.librepass.android.common.popUpToDestination
import dev.medzik.librepass.android.database.Repository
import dev.medzik.librepass.android.ui.LibrePassNavigation
import dev.medzik.librepass.android.ui.screens.auth.Unlock
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.android.utils.ThemeValues
import dev.medzik.librepass.android.utils.Vault
import org.apache.commons.lang3.exception.ExceptionUtils
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var vault: Vault

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("LibrePass", "Uncaught exception", e)

            openEmailApplication(
                email = "contact@librepass.org",
                subject = "[Bug] [Android]: ",
                body = "<A few words about the error>\n\n\n---- Stack trace for debugging ----\n\n${ExceptionUtils.getStackTrace(e)}"
            )

            finish()
        }

        // retrieves aes key to decrypt vault if key is valid
        vault.getVaultSecrets(this)

        // merge application data when application updated
        Migrations.update(this, repository)

        // get app theme settings
        val dynamicColor = readKey(StoreKey.DynamicColor)
        val theme = readKey(StoreKey.Theme)
        val autoTheme = theme == ThemeValues.SYSTEM.ordinal
        val darkTheme = theme == ThemeValues.DARK.ordinal
        val blackTheme = theme == ThemeValues.BLACK.ordinal

        setContent {
            LibrePassTheme(
                darkTheme = blackTheme || darkTheme || (autoTheme && isSystemInDarkTheme()),
                blackTheme = blackTheme,
                dynamicColor = dynamicColor
            ) {
                LibrePassNavigation()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // check if user is logged
        if (repository.credentials.get() == null) return

        vault.saveVaultExpiration(this)
    }

    /** Called from [LibrePassNavigation]. */
    fun onResume(navController: NavController) {
        // check if user is logged
        if (repository.credentials.get() == null) return

        val expired = vault.handleExpiration(this)
        if (expired) {
            navController.navigate(
                Unlock
            ) {
                popUpToDestination(navController)
            }
        }
    }
}
