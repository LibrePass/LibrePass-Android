package dev.medzik.librepass.android.ui.screens

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
import dev.medzik.librepass.client.api.v1.AuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    // get composable context
    val context = LocalContext.current

    // coroutine scope
    val scope = rememberCoroutineScope()

    // login data
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    // error states
    val isEmailError = email.value.isNotEmpty() && !email.value.contains("@")
    val isPasswordError = password.value.isNotEmpty() && password.value.length < 8

    // loading state
    var loading by remember { mutableStateOf(false) }

    // snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // API client
    val authClient = AuthClient()

    /**
     * Login user with given credentials and navigate to dashboard.
     */
    fun onLogin(email: String, password: String) {
        if (isEmailError || isPasswordError) {
            return
        }

        // set loading state
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                val basePassword = AuthClient.computeBasePasswordHash(
                    password = password,
                    email = email
                ).toHexHash()

                val credentials = authClient.login(
                    email = email,
                    password = password
//                    passwordIsBaseHash = true
                )

                val repository = Repository(context = context)

                repository.credentials.insert(
                    Credentials(
                        userId = credentials.userId,
                        email = email,
                        accessToken = credentials.accessToken,
                        refreshToken = credentials.refreshToken,
                        encryptionKey = credentials.encryptionKey
                    )
                )

                val encryptionKey = AesCbc.decrypt(
                    credentials.encryptionKey,
                    basePassword
                )

                // navigate to dashboard
                scope.launch(Dispatchers.Main) {
                    navController.navigate(
                        Screen.Dashboard.fill(
                            Argument.EncryptionKey to encryptionKey
                        )
                    ) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                // TODO: handle error for invalid credentials and network error
//                scope.launch { snackbarHostState.showSnackbar("Invalid credentials") }
                scope.launch { snackbarHostState.showSnackbar(e.toString()) }
            }

            // set loading state
            loading = false
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
                state = email,
                isError = isEmailError,
                errorMessage = stringResource(id = R.string.invalid_email),
                keyboardType = KeyboardType.Email
            )

            TextInputField(
                label = stringResource(id = R.string.password),
                state = password,
                hidden = true,
                isError = isPasswordError,
                errorMessage = stringResource(id = R.string.invalid_password_too_short),
                keyboardType = KeyboardType.Password
            )

            Button(
                onClick = { onLogin(email.value, password.value) },
                enabled =
                !isEmailError && !isPasswordError &&
                    email.value.isNotEmpty() && password.value.isNotEmpty() &&
                    !loading,
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
