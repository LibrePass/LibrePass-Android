package dev.medzik.librepass.android.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import dev.medzik.android.components.PreferenceEntry
import dev.medzik.android.components.navigate
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Screen
import kotlinx.coroutines.runBlocking

@Composable
fun SettingsAccountScreen(navController: NavController) {
    val context = LocalContext.current

    val repository = context.getRepository()

    fun logout() =
        runBlocking {
            val credentials = repository.credentials.get()!!

            repository.credentials.drop()
            repository.cipher.drop(credentials.userId)

            navController.navigate(
                screen = Screen.Welcome,
                disableBack = true
            )
        }

    PreferenceEntry(
        title = stringResource(R.string.Settings_Logout),
        icon = { Icon(Icons.Default.Logout, contentDescription = null) },
        onClick = { logout() },
    )
}
