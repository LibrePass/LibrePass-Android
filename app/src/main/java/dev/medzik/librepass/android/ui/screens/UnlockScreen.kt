package dev.medzik.librepass.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.libcrypto.AesCbc
import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composable.LoadingIndicator
import dev.medzik.librepass.android.ui.composable.TextInputField
import dev.medzik.librepass.android.ui.composable.TopBar
import dev.medzik.librepass.client.api.v1.PasswordIterations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UnlockScreen(navController: NavController) {
    val password = remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    val loading = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    fun onUnlock(password: String) {
        loading.value = true

        val repository = Repository(context = context)

        scope.launch(Dispatchers.IO) {
            try {
                val dbCredentials = repository.get()!!

                val encryptedEncryptionKey = dbCredentials.encryptionKey

                val basePassword = Pbkdf2(PasswordIterations).sha256(password, dbCredentials.email.encodeToByteArray())

                val encryptionKey = AesCbc.decrypt(
                    encryptedEncryptionKey,
                    basePassword,
                )

                scope.launch(Dispatchers.Main) { navController.navigate(Screen.Dashboard(encryptionKey)) }
            } catch (e: Exception) {
                loading.value = false

                snackbarHostState.showSnackbar(e.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = stringResource(id = R.string.login))
        },
        modifier = Modifier.navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp) // TopBar padding
                .padding(top = 20.dp)
                .padding(horizontal = 16.dp),
        ) {
            TextInputField(
                label = stringResource(id = R.string.password),
                state = password,
                hidden = true,
                keyboardType = KeyboardType.Password,
            )

            Button(
                onClick = { onUnlock(password.value) },
                enabled = password.value.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 40.dp)
            ) {
                if (loading.value) LoadingIndicator(animating = true)
                else Text(text = stringResource(id = R.string.login_button))
            }
        }
    }
}
