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
import dev.medzik.librepass.android.data.Credentials
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.utils.SecretStore
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.android.utils.UserSecrets
import dev.medzik.librepass.android.utils.exception.handle
import dev.medzik.librepass.android.utils.navigation.navigate
import dev.medzik.librepass.android.utils.rememberLoadingState
import dev.medzik.librepass.android.utils.rememberStringData
import dev.medzik.librepass.android.utils.runGC
import dev.medzik.librepass.android.utils.showToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.AuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var loading by rememberLoadingState()
    var email by rememberStringData()
    var password by rememberStringData()
    var server by rememberStringData(Server.PRODUCTION)

    val credentialsRepository = context.getRepository().credentials

    fun submit(email: String, password: String) {
        val authClient = AuthClient(apiUrl = server)

        if (email.isEmpty() || password.isEmpty())
            return

        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                val preLogin = authClient.preLogin(email)

                val credentials = authClient.login(
                    email = email,
                    password = password
                )

                // run gc cycle after computing password hash
                runGC()

                // save credentials
                credentialsRepository.insert(
                    Credentials(
                        userId = credentials.userId,
                        email = email,
                        apiUrl = if (server == Server.PRODUCTION) null else server,
                        apiKey = credentials.apiKey,
                        publicKey = credentials.keyPair.publicKey,
                        // Argon2id parameters
                        memory = preLogin.memory,
                        iterations = preLogin.iterations,
                        parallelism = preLogin.parallelism
                    )
                )

                // save secrets in encrypted datastore
                SecretStore.save(
                    context,
                    UserSecrets(
                        privateKey = credentials.keyPair.privateKey,
                        secretKey = credentials.secretKey
                    )
                )

                // navigate to dashboard
                scope.launch(Dispatchers.Main) {
                    navController.navigate(
                        screen = Screen.Dashboard,
                        disableBack = true
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
                title = R.string.TopBar_Login,
                navigationIcon = { TopBarBackIcon(navController) }
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
                keyboardType = KeyboardType.Email
            )

            Text(
                text = stringResource(R.string.Auth_Get_Password_Hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable {
                        val authClient = AuthClient(apiUrl = server)

                        if (email.isEmpty()) {
                            context.showToast(context.getString(R.string.Toast_Enter_Email))
                            return@clickable
                        }

                        scope.launch(Dispatchers.IO) {
                            try {
                                authClient.requestPasswordHint(email)

                                context.showToast(context.getString(R.string.Toast_Password_Hint_Sent))
                            } catch (e: Exception) {
                                e.handle(context)
                            }
                        }
                    }
            )

            TextInputField(
                label = R.string.InputField_Password,
                value = password,
                onValueChange = { password = it },
                hidden = true,
                keyboardType = KeyboardType.Password
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
                    text = stringResource(R.string.Server_AuthScreen_Server_Address) + ": ",
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
                enabled = email.isNotEmpty() && password.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            ) {
                Text(stringResource(R.string.Button_Login))
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
