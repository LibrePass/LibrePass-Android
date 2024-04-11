package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import dev.medzik.android.components.PickerDialog
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberDialogState
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.components.rememberMutableString
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.BuildConfig
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.components.TextInputField
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.StoreKey
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
    var server by rememberMutableString(Server.PRODUCTION)

    // Register user with given credentials and navigate to log in screen.
    fun submit(email: String, password: String) {
        val authClient = AuthClient(apiUrl = server)

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

    val serverChoiceDialog = rememberDialogState()

    @Composable
    fun getServerName(server: String): String {
        return when (server) {
            Server.PRODUCTION -> {
                stringResource(R.string.Server_Official)
            }

            Server.TEST -> {
                stringResource(R.string.Server_Testing)
            }

            else -> server
        }
    }

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable { serverChoiceDialog.show() }
    ) {
        Text(
            text = stringResource(R.string.ServerAddress) + ": ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Text(
            text = getServerName(server),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }

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

    var servers = listOf(Server.PRODUCTION)
        .plus(context.readKey(StoreKey.CustomServers))
        .plus("custom_server")

    if (BuildConfig.DEBUG) servers = servers.plus(Server.TEST)

    PickerDialog(
        state = serverChoiceDialog,
        title = stringResource(R.string.ServerAddress),
        items = servers,
        onSelected = {
            if (it == "custom_server") {
                navController.navigate(Screen.AddCustomServer)
            } else {
                server = it
            }
        }
    ) {
        Text(
            text = when (it) {
                "custom_server" -> {
                    stringResource(R.string.Server_Choice_Dialog_AddCustom)
                }

                else -> getServerName(it)
            },
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
        )
    }
}
