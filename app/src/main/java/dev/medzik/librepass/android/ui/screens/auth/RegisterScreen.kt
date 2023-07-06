package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import dev.medzik.android.composables.LoadingIndicator
import dev.medzik.android.composables.TextInputField
import dev.medzik.android.composables.TopBar
import dev.medzik.android.composables.TopBarBackIcon
import dev.medzik.android.composables.res.Text
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.android.utils.Navigation.navigate
import dev.medzik.librepass.android.utils.Remember.rememberLoadingState
import dev.medzik.librepass.android.utils.Remember.rememberSnackbarHostState
import dev.medzik.librepass.android.utils.Remember.rememberStringData
import dev.medzik.librepass.android.utils.Toast.showToast
import dev.medzik.librepass.android.utils.exception.handle
import dev.medzik.librepass.client.api.v1.AuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val snackbarHostState = rememberSnackbarHostState()

    var loading by rememberLoadingState()
    var email by rememberStringData()
    var password by rememberStringData()
    var configPassword by rememberStringData()
    var passwordHint by rememberStringData()

    // error states
    val isEmailError = email.isNotEmpty() && !email.contains("@")
    val isPasswordError = password.isNotEmpty() && password.length < 8

    val authClient = AuthClient()

    // Register user with given credentials and navigate to login screen.
    fun submit(email: String, password: String) {
        // disable button
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                authClient.register(email, password)

                // navigate to login
                scope.launch(Dispatchers.Main) {
                    context.showToast(R.string.Success_Registration_Please_Verify)

                    navController.navigate(
                        screen = Screen.Login,
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
                title = stringResource(R.string.TopBar_Register),
                navigationIcon = {
                    TopBarBackIcon(navController)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TextInputField(
                label = stringResource(R.string.InputField_Email),
                value = email,
                onValueChange = { email = it },
                isError = isEmailError,
                errorMessage = stringResource(R.string.Error_InvalidEmail),
                keyboardType = KeyboardType.Email
            )

            TextInputField(
                label = stringResource(R.string.InputField_Password),
                value = password,
                onValueChange = { password = it },
                hidden = true,
                isError = isPasswordError,
                errorMessage = stringResource(R.string.Error_InvalidPasswordTooShort),
                keyboardType = KeyboardType.Password
            )

            TextInputField(
                label = stringResource(R.string.InputField_ConfirmPassword),
                value = configPassword,
                onValueChange = { configPassword = it },
                hidden = true,
                isError = configPassword.isNotEmpty() && configPassword != password,
                errorMessage = stringResource(R.string.Error_PasswordsDoNotMatch),
                keyboardType = KeyboardType.Password
            )

            TextInputField(
                label = "${stringResource(R.string.InputField_PasswordHint)} (${stringResource(R.string.InputField_Optional)})",
                value = passwordHint,
                onValueChange = { passwordHint = it },
                keyboardType = KeyboardType.Text
            )

            Button(
                onClick = { submit(email, password) },
                // disable button if there are any errors or loading is in progress
                enabled = !isEmailError && !isPasswordError && configPassword == password && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 40.dp)
            ) {
                if (loading)
                    LoadingIndicator(animating = true)
                else
                    Text(R.string.Button_Register)
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
