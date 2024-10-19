package dev.medzik.librepass.android

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import dagger.hilt.android.AndroidEntryPoint
import dev.medzik.android.utils.openEmailApplication
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.business.VaultCache
import dev.medzik.librepass.android.common.popUpToDestination
import dev.medzik.librepass.android.database.Repository
import dev.medzik.librepass.android.ui.LibrePassNavigation
import dev.medzik.librepass.android.ui.screens.auth.Unlock
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var vault: VaultCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("LibrePass", "Uncaught exception", e)

            openEmailApplication(
                email = "contact@librepass.org",
                subject = "[Bug] [Android]: ",
                body = "<A few words about the error>\n\n\n---- Stack trace for debugging ----\n\n${Log.getStackTraceString(e)}"
            )

            finish()
        }

        MigrationsManager.run(this, repository)

        // retrieves aes key for vault decryption if key is valid
        vault.getSecretsIfNotExpired(this)

        this.showToast("This application is deprecated.")

        setContent {
            LibrePassTheme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = true
            ) {
                var showDeprecatedWarning by remember { mutableStateOf(true) }
                if (showDeprecatedWarning) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeprecatedWarning = false
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeprecatedWarning = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        title = {
                            Text("Warning")
                        },
                        text = {
                            Text("This application is deprecated. Please migrate all passwords before December 2024")
                        }
                    )
                }

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
            navController.navigate(Unlock) {
                popUpToDestination(Unlock)
            }
        }
    }
}
