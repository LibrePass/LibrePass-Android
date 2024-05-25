package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.android.components.LoadingButton
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.components.rememberMutableString
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.components.TextInputField
import dev.medzik.librepass.android.ui.components.auth.ChoiceServer
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.AuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var loading by rememberMutableBoolean()
    var email by rememberMutableString()
    var password by rememberMutableString()
    var configPassword by rememberMutableString()
    var passwordHint by rememberMutableString()
    val server = rememberMutableString(Server.PRODUCTION)

    // Register user with given credentials and navigate to log in screen.
    fun submit(email: String, password: String) {
        val authClient = AuthClient(apiUrl = server.value)

        // disable button
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                authClient.register(email, password, passwordHint)

                // navigate to login
                runOnUiThread {
                    context.showToast(R.string.Toast_PleaseVerifyYourEmail)

                    navController.navigate(
                        screen = Screen.Login,
                        disableBack = true
                    )
                }
            } catch (e: Exception) {
                loading = false
                e.showErrorToast(context)
            }
        }
    }

    TextInputField(
        label = stringResource(R.string.Email),
        value = email,
        onValueChange = { email = it },
        isError = email.isNotEmpty() && !email.contains("@"),
        errorMessage = stringResource(R.string.Error_InvalidEmail),
        keyboardType = KeyboardType.Email
    )

    TextInputField(
        label = stringResource(R.string.Password),
        value = password,
        onValueChange = { password = it },
        hidden = true,
        isError = password.isNotEmpty() && password.length < 8,
        errorMessage = stringResource(R.string.Error_PasswordTooShort),
        keyboardType = KeyboardType.Password
    )

    TextInputField(
        label = stringResource(R.string.ConfirmPassword),
        value = configPassword,
        onValueChange = { configPassword = it },
        hidden = true,
        isError = configPassword.isNotEmpty() && configPassword != password,
        errorMessage = stringResource(R.string.Error_PasswordsDoNotMatch),
        keyboardType = KeyboardType.Password
    )

    TextInputField(
        label = "${stringResource(R.string.PasswordHint)} (${stringResource(R.string.optional)})",
        value = passwordHint,
        onValueChange = { passwordHint = it },
        keyboardType = KeyboardType.Text
    )

    ChoiceServer(navController, server)

    LoadingButton(
        loading = loading,
        onClick = { submit(email, password) },
        enabled = email.contains("@") && password.length >= 8,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
    ) {
        Text(stringResource(R.string.Register))
    }
}
