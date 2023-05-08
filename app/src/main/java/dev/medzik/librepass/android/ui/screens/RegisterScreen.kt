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
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.common.LoadingIndicator
import dev.medzik.librepass.android.ui.composables.common.TextInputField
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.client.api.v1.AuthClient
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    // get composable context
    val context = LocalContext.current

    // coroutine scope
    val scope = rememberCoroutineScope()

    // register data
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val configPassword = remember { mutableStateOf("") }
    val passwordHint = remember { mutableStateOf("") }

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
     * Register user with given credentials and navigate to login screen.
     */
    fun onLogin(email: String, password: String) {
        if (isEmailError || isPasswordError || configPassword.value != password) {
            return
        }

        // disable button
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                authClient.register(email, password)

                // navigate to login
                scope.launch(Dispatchers.Main) {
                    navController.navigate(Screen.Login.get) {
                        // disable back navigation
                        popUpTo(Screen.Login.get) { inclusive = true }
                    }
                }
            } catch (e: ClientException) {
                // Handle network error
                scope.launch {
                    snackbarHostState.showSnackbar(
                        context.resources.getString(R.string.network_error)
                    )
                }
            } catch (e: ApiException) {
                // Handle API error
                // TODO: user already exists and more
                scope.launch {
                    snackbarHostState.showSnackbar(
                        context.resources.getString(R.string.user_already_exists)
                    )
                }
            }

            // enable button
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = R.string.register),
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

            TextInputField(
                label = stringResource(id = R.string.confirm_password),
                state = configPassword,
                hidden = true,
                isError = configPassword.value.isNotEmpty() && configPassword.value != password.value,
                errorMessage = stringResource(id = R.string.passwords_do_not_match),
                keyboardType = KeyboardType.Password
            )

            TextInputField(
                label = "${stringResource(id = R.string.password_hint)} (${stringResource(id = R.string.optional)})",
                state = passwordHint,
                keyboardType = KeyboardType.Text
            )

            Button(
                onClick = { onLogin(email.value, password.value) },
                enabled =
                !isEmailError && !isPasswordError &&
                    email.value.isNotEmpty() && password.value.isNotEmpty() &&
                    !loading &&
                    configPassword.value == password.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 40.dp)
            ) {
                if (loading) {
                    LoadingIndicator(animating = true)
                } else {
                    Text(text = stringResource(id = R.string.register_button))
                }
            }
        }
    }
}

@Preview
@Composable
fun RegisterPreview() {
    LibrePassTheme {
        RegisterScreen(NavHostController(LocalContext.current))
    }
}
