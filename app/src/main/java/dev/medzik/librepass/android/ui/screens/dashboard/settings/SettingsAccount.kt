package dev.medzik.librepass.android.ui.screens.dashboard.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import dev.medzik.android.composables.TopBar
import dev.medzik.android.composables.TopBarBackIcon
import dev.medzik.android.composables.settings.SettingsProperty
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.utils.navigation.navigate
import kotlinx.coroutines.runBlocking

@Composable
fun SettingsAccount(navController: NavController) {
    val context = LocalContext.current

    val repository = context.getRepository()

    fun logout() = runBlocking {
        val credentials = repository.credentials.get()!!

        repository.credentials.drop()
        repository.cipher.drop(credentials.userId)

        navController.navigate(
            screen = Screen.Welcome,
            disableBack = true
        )
    }

    Scaffold(
        topBar = {
            TopBar(
                title = R.string.Settings_Group_Account,
                navigationIcon = { TopBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            SettingsProperty(
                icon = Icons.Default.Logout,
                resId = R.string.Settings_Logout,
                onClick = { logout() },
            )
        }
    }
}
