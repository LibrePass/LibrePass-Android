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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composable.LoadingIndicator
import dev.medzik.librepass.android.ui.composable.TextInputField
import dev.medzik.librepass.android.ui.composable.TopBar
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.client.api.v1.AuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val scope = rememberCoroutineScope()

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val configPassword = remember { mutableStateOf("") }
    val passwordHint = remember { mutableStateOf("") }

    val isEmailError = email.value.isNotEmpty() && !email.value.contains("@")
    val isPasswordError = password.value.isNotEmpty() && password.value.length < 8

    val authClient = AuthClient()

    val loading = remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    fun onLogin(email: String, password: String) {
        if (isEmailError || isPasswordError || configPassword.value != password) {
            return
        }

        loading.value = true

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
            } catch (e: Throwable) {
                scope.launch(Dispatchers.Main) {
//                    snackbarHostState.showSnackbar("Unexpected error occurred")
                    snackbarHostState.showSnackbar(message = e.message ?: "Unknown error")
                }
            }

            loading.value = false
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = stringResource(id = R.string.register))
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
                label = stringResource(id = R.string.email),
                state = email,
                isError = isEmailError,
                errorMessage = stringResource(id = R.string.invalid_email),
                keyboardType = KeyboardType.Email,
            )

            TextInputField(
                label = stringResource(id = R.string.password),
                state = password,
                hidden = true,
                isError = isPasswordError,
                errorMessage = stringResource(id = R.string.invalid_password_too_short),
                keyboardType = KeyboardType.Password,
            )

            TextInputField(
                label = stringResource(id = R.string.confirm_password),
                state = configPassword,
                hidden = true,
                isError = configPassword.value.isNotEmpty() && configPassword.value != password.value,
                errorMessage = stringResource(id = R.string.passwords_do_not_match),
                keyboardType = KeyboardType.Password,
            )

            TextInputField(
                label = "${stringResource(id = R.string.password_hint)} (${stringResource(id = R.string.optional)})",
                state = passwordHint,
                keyboardType = KeyboardType.Text,
            )

            Button(
                onClick = { onLogin(email.value, password.value) },
                enabled =
                    !isEmailError && !isPasswordError
                    && email.value.isNotEmpty() && password.value.isNotEmpty()
                    && !loading.value
                    && configPassword.value == password.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 40.dp)
            ) {
                if (loading.value) LoadingIndicator(animating = true)
                else Text(text = stringResource(id = R.string.register_button))
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
