package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import dev.medzik.android.composables.LoadingButton
import dev.medzik.android.composables.TextInputField
import dev.medzik.android.composables.TopBar
import dev.medzik.android.composables.TopBarBackIcon
import dev.medzik.android.composables.dialog.PickerDialog
import dev.medzik.android.composables.dialog.rememberDialogState
import dev.medzik.librepass.android.BuildConfig
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.utils.Navigation.navigate
import dev.medzik.librepass.android.utils.Remember.rememberLoadingState
import dev.medzik.librepass.android.utils.Remember.rememberStringData
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.android.utils.Toast.showToast
import dev.medzik.librepass.android.utils.exception.handle
import dev.medzik.librepass.android.utils.runGC
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.AuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var loading by rememberLoadingState()
    var email by rememberStringData()
    var password by rememberStringData()
    var configPassword by rememberStringData()
    var passwordHint by rememberStringData()
    var server by rememberStringData(Server.PRODUCTION)

    // Register user with given credentials and navigate to log in screen.
    fun submit(email: String, password: String) {
        val authClient = AuthClient(apiUrl = server)

        // disable button
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                authClient.register(email, password, passwordHint)

                // run gc cycle after computing password hash
                runGC()

                // navigate to login
                scope.launch(Dispatchers.Main) {
                    context.showToast(R.string.Success_Registration_Please_Verify)

                    navController.navigate(
                        screen = Screen.Login,
                        options = {
                            // disable back to this page
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    )
                }
            } catch (e: Exception) {
                loading = false

                e.handle(context)
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = R.string.TopBar_Register,
                navigationIcon = {
                    TopBarBackIcon(navController)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TextInputField(
                label = R.string.InputField_Email,
                value = email,
                onValueChange = { email = it },
                isError = email.isNotEmpty() && !email.contains("@"),
                errorMessage = stringResource(R.string.Error_InvalidEmail),
                keyboardType = KeyboardType.Email
            )

            TextInputField(
                label = R.string.InputField_Password,
                value = password,
                onValueChange = { password = it },
                hidden = true,
                isError = password.isNotEmpty() && password.length < 8,
                errorMessage = stringResource(R.string.Error_InvalidPasswordTooShort),
                keyboardType = KeyboardType.Password
            )

            TextInputField(
                label = R.string.InputField_ConfirmPassword,
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

            val serverChoiceDialog = rememberDialogState()

            @Composable
            fun getServerName(server: String): String {
                return when (server) {
                    Server.PRODUCTION -> {
                        stringResource(R.string.Server_Choice_Dialog_Official)
                    }

                    Server.TEST -> {
                        stringResource(R.string.Server_Choice_Dialog_Testing)
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
                    text = "Server: ",
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
                Text(stringResource(R.string.Button_Register))
            }

            var servers = listOf(Server.PRODUCTION)
                .plus(context.readKey(StoreKey.CustomServers))
                .plus("custom_server")

            if (BuildConfig.DEBUG)
                servers = servers.plus(Server.TEST)

            PickerDialog(
                state = serverChoiceDialog,
                title = R.string.Server_Choice_Dialog_Title,
                items = servers,
                onSelected = {
                    if (it == "custom_server") {
                        navController.navigate(Screen.AddCustomServer)
                    } else server = it
                }
            ) {
                val text = when (it) {
                    "custom_server" -> {
                        stringResource(R.string.Server_Choice_Dialog_Add_Custom)
                    }

                    else -> getServerName(it)
                }

                Text(
                    text = text,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}
