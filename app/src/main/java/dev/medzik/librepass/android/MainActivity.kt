package dev.medzik.librepass.android

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.medzik.librepass.android.database.Repository
import dev.medzik.librepass.android.ui.LibrePassNavigation
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("LibrePass", "Uncaught exception", e)

            // TODO

            finish()
        }

        MigrationsManager.run(this, repository)

        // retrieves aes key for vault decryption if key is valid
//        vault.getSecretsIfNotExpired(this)

        setContent {
            LibrePassTheme {
                LibrePassNavigation()
            }
        }
    }

//    override fun onPause() {
//        super.onPause()
//
//        // check if user is logged
//        if (repository.credentials.get() == null) return
//
//        vault.saveVaultExpiration(this)
//    }
//
//    /** Called from [LibrePassNavigation]. */
//    fun onResume(navController: NavController) {
//        // check if user is logged
//        if (repository.credentials.get() == null) return
//
//        val expired = vault.handleExpiration(this)
//        if (expired) {
//            navController.navigate(Unlock) {
//                popUpToDestination(Unlock)
//            }
//        }
//    }
}
