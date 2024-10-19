package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
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
import dev.medzik.android.compose.rememberMutable
import dev.medzik.android.compose.ui.LoadingButton
import dev.medzik.android.compose.ui.textfield.AnimatedTextField
import dev.medzik.android.compose.ui.textfield.PasswordAnimatedTextField
import dev.medzik.android.compose.ui.textfield.TextFieldValue
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.common.haveNetworkConnection
import dev.medzik.librepass.android.common.popUpToStartDestination
import dev.medzik.librepass.android.ui.components.auth.ChoiceServer
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.AuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object Register

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var loading by rememberMutable(false)
    val email = rememberMutable("")
    val password = rememberMutable("")
    val confirmPassword = rememberMutable("")
    val passwordHint = rememberMutable("")
    val server = rememberMutable(Server.PRODUCTION)

    // Register user with given credentials and navigate to log in screen.
    fun submit(email: String, password: String, passwordHint: String?) {
        if (!context.haveNetworkConnection()) {
            context.showToast(R.string.Error_NoInternetConnection)
            return
        }

        val authClient = AuthClient(apiUrl = server.value)

        // disable button
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                authClient.register(email, password, passwordHint)

                // navigate to login
                runOnUiThread {
                    context.showToast(R.string.Toast_PleaseVerifyYourEmail)

                    navController.navigate(Login) {
                        popUpToStartDestination(navController)
                    }
                }
            } catch (e: Exception) {
                loading = false
                e.showErrorToast(context)
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        AnimatedTextField(
            label = stringResource(R.string.Email),
            value = TextFieldValue.fromMutableState(email),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            leading = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null
                )
            }
        )

        PasswordAnimatedTextField(
            label = stringResource(R.string.Password),
            value = TextFieldValue.fromMutableState(
                state = password,
                error = if (password.value.isNotEmpty() && password.value.length < 8) {
                    stringResource(R.string.Error_PasswordTooShort)
                } else null
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        PasswordAnimatedTextField(
            label = stringResource(R.string.ConfirmPassword),
            value = TextFieldValue.fromMutableState(
                state = confirmPassword,
                error = if (confirmPassword.value.isNotEmpty() && password.value != confirmPassword.value) {
                    stringResource(R.string.Error_PasswordsDoNotMatch)
                } else null
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        AnimatedTextField(
            label = stringResource(R.string.PasswordHint),
            value = TextFieldValue.fromMutableState(
                passwordHint,
                valueLabel = TextFieldValue.ValueLabel(
                    type = TextFieldValue.ValueLabel.Type.INFO,
                    text = stringResource(R.string.Optional)
                )
            ),
            leading = {
                Icon(
                    Icons.Default.QuestionMark,
                    contentDescription = null
                )
            }
        )
    }

    ChoiceServer(navController, server)

    val isError = !email.value.contains("@") ||
            password.value.length < 8 ||
            confirmPassword.value != password.value

    LoadingButton(
        loading = loading,
        onClick = { submit(email.value, password.value, passwordHint.value) },
        enabled = !isError,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
    ) {
        Text(stringResource(R.string.Register))
    }
}
