package dev.medzik.librepass.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import dev.medzik.libcrypto.AesCbc
import dev.medzik.libcrypto.EncryptException
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.common.LoadingIndicator
import dev.medzik.librepass.android.ui.composables.common.TextInputField
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.utils.KeyStoreAlias
import dev.medzik.librepass.android.utils.KeyStoreUtils
import dev.medzik.librepass.android.utils.showBiometricPrompt
import dev.medzik.librepass.client.api.v1.AuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UnlockScreen(navController: NavController) {
    // get composable context
    val context = LocalContext.current as FragmentActivity

    // password state
    val password = remember { mutableStateOf("") }

    // snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // loading state
    var loading by remember { mutableStateOf(false) }

    // coroutine scope
    val scope = rememberCoroutineScope()

    // get credentials from database
    val repository = Repository(context = context)
    val dbCredentials = repository.credentials.get()!!
    val encryptedEncryptionKey = dbCredentials.encryptionKey

    fun onUnlock(password: String) {
        // disable button
        loading = true

        lateinit var encryptionKey: String

        scope.launch(Dispatchers.IO) {
            try {
                loading = true

                // compute base password hash
                // TODO: set argon2 params
                val basePassword = AuthClient
                    .computeBasePasswordHash(password, dbCredentials.email)
                    .toHexHash()

                // decrypt encryption key
                encryptionKey = AesCbc.decrypt(
                    encryptedEncryptionKey,
                    basePassword
                )
            } catch (e: EncryptException) {
                // if password is invalid
                loading = false
                snackbarHostState.showSnackbar(context.getString(R.string.invalid_credentials))
            } finally {
                repository.credentials.update(
                    dbCredentials.copy(requireRefresh = true)
                )

                // run only if loading is true (if no error occurred)
                if (loading) {
                    scope.launch(Dispatchers.Main) {
                        navController.navigate(
                            Screen.Dashboard.fill(
                                Argument.EncryptionKey to encryptionKey
                            )
                        ) {
                            // disable back navigation
                            popUpTo(Screen.Unlock.get) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    fun showBiometric() {
        showBiometricPrompt(
            context = context,
            cipher = KeyStoreUtils.getCipherForDecryption(
                alias = KeyStoreAlias.ENCRYPTION_KEY.name,
                initializationVector = dbCredentials.biometricEncryptionKeyIV!!
            ),
            onAuthenticationSucceeded = { cipher ->
                val encryptionKey = KeyStoreUtils.decrypt(
                    cipher = cipher,
                    data = dbCredentials.biometricEncryptionKey!!
                )

                scope.launch(Dispatchers.IO) {
                    repository.credentials.update(
                        dbCredentials.copy(requireRefresh = true)
                    )

                    scope.launch(Dispatchers.Main) {
                        navController.navigate(
                            Screen.Dashboard.fill(
                                Argument.EncryptionKey to encryptionKey
                            )
                        ) {
                            // disable back navigation
                            popUpTo(Screen.Unlock.get) { inclusive = true }
                        }
                    }
                }
            },
            onAuthenticationFailed = { }
        )
    }

    LaunchedEffect(scope) {
        if (dbCredentials.biometricEnabled) {
            showBiometric()
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = stringResource(id = R.string.unlock))
        },
        modifier = Modifier.navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TextInputField(
                label = stringResource(id = R.string.password),
                state = password,
                hidden = true,
                keyboardType = KeyboardType.Password
            )

            Button(
                onClick = { onUnlock(password.value) },
                enabled = password.value.isNotEmpty() && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 80.dp)
            ) {
                if (loading) {
                    LoadingIndicator(animating = true)
                } else {
                    Text(text = stringResource(id = R.string.unlock_button))
                }
            }

            if (dbCredentials.biometricEnabled) {
                OutlinedButton(
                    onClick = { showBiometric() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .padding(horizontal = 80.dp)
                ) {
                    Text(text = stringResource(id = R.string.use_biometric))
                }
            }
        }
    }
}
