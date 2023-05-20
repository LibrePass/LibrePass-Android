package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import dev.medzik.libcrypto.AesCbc
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.Credentials
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.common.LoadingIndicator
import dev.medzik.librepass.android.ui.composables.common.TextInputField
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.android.utils.exception.handle
import dev.medzik.librepass.android.utils.navigation.navigate
import dev.medzik.librepass.client.api.v1.AuthClient
import dev.medzik.librepass.client.utils.Cryptography.computeBasePasswordHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    // get composable context
    val context = LocalContext.current

    // repository
    val repository = Repository(context = context)

    // login data
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // loading state
    var loading by remember { mutableStateOf(false) }

    // coroutine scope
    val scope = rememberCoroutineScope()
    // snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // API client
    val authClient = AuthClient()

    /**
     * Login user with given credentials and navigate to dashboard.
     */
    fun onLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            return
        }

        // set loading state
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                // compute base password hash
                val basePassword = computeBasePasswordHash(
                    password = password,
                    email = email
                ).toHexHash()

                // authenticate user and get credentials
                val credentials = authClient.login(
                    email = email,
                    password = password
                )

                // get argon2id parameters
                val argon2idParameters = authClient.getUserArgon2idParameters(email)

                // insert credentials into local database
                repository.credentials.insert(
                    Credentials(
                        userId = credentials.userId,
                        email = email,
                        accessToken = credentials.accessToken,
                        encryptionKey = credentials.encryptionKey,
                        // argon2id parameters
                        memory = argon2idParameters.memory,
                        iterations = argon2idParameters.iterations,
                        parallelism = argon2idParameters.parallelism,
                        version = argon2idParameters.version
                    )
                )

                // decrypt encryption key
                val encryptionKey = AesCbc.decrypt(
                    credentials.encryptionKey,
                    basePassword
                )

                // navigate to dashboard
                scope.launch(Dispatchers.Main) {
                    navController.navigate(
                        screen = Screen.Dashboard,
                        argument = Argument.EncryptionKey to encryptionKey,
                        disableBack = true
                    )
                }
            } catch (e: Exception) {
                loading = false

                e.handle(context, snackbarHostState)
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = R.string.login),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
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
                label = stringResource(id = R.string.email),
                value = email,
                onValueChange = { email = it },
                isError = email.isEmpty(),
                errorMessage = stringResource(id = R.string.invalid_email),
                keyboardType = KeyboardType.Email
            )

            TextInputField(
                label = stringResource(id = R.string.password),
                value = password,
                onValueChange = { password = it },
                hidden = true,
                isError = password.isEmpty(),
                errorMessage = stringResource(id = R.string.invalid_password_too_short),
                keyboardType = KeyboardType.Password
            )

            Button(
                onClick = { onLogin(email, password) },
                // disable button if email or password is empty or loading is in progress
                enabled = email.isNotEmpty() && password.isNotEmpty() && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 40.dp)
            ) {
                if (loading) {
                    LoadingIndicator(animating = true)
                } else {
                    Text(text = stringResource(id = R.string.login_button))
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    LibrePassTheme {
        LoginScreen(NavHostController(LocalContext.current))
    }
}
