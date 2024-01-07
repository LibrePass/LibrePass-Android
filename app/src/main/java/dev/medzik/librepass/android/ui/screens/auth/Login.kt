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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.components.LoadingButton
import dev.medzik.android.components.PickerDialog
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberDialogState
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.components.rememberMutableString
import dev.medzik.android.crypto.KeyStore
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.BuildConfig
import dev.medzik.librepass.android.MainActivity
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.Credentials
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.components.TextInputField
import dev.medzik.librepass.android.utils.KeyAlias
import dev.medzik.librepass.android.utils.SecretStore
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.android.utils.UserSecrets
import dev.medzik.librepass.android.utils.checkIfBiometricAvailable
import dev.medzik.librepass.android.utils.showBiometricPromptForSetup
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.AuthClient
import dev.medzik.librepass.utils.fromHexString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var loading by rememberMutableBoolean()
    var email by rememberMutableString()
    var password by rememberMutableString()
    var server by rememberMutableString(Server.PRODUCTION)

    fun submit(
        email: String,
        password: String
    ) {
        val authClient = AuthClient(apiUrl = server)

        if (email.isEmpty() || password.isEmpty())
            return

        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                val preLogin = authClient.preLogin(email)

                val credentials =
                    authClient.login(
                        email = email,
                        password = password
                    )

                // save credentials
                val credentialsDb =
                    Credentials(
                        userId = credentials.userId,
                        email = email,
                        apiUrl = if (server == Server.PRODUCTION) null else server,
                        apiKey = credentials.apiKey,
                        publicKey = credentials.publicKey,
                        // Argon2id parameters
                        memory = preLogin.memory,
                        iterations = preLogin.iterations,
                        parallelism = preLogin.parallelism
                    )
                viewModel.credentialRepository.insert(credentialsDb)

                // save secrets in encrypted datastore
                SecretStore.save(
                    context,
                    UserSecrets(
                        privateKey = credentials.privateKey.fromHexString(),
                        secretKey = credentials.secretKey.fromHexString()
                    )
                )

                runOnUiThread {
                    // enable biometric authentication if possible
                    if (checkIfBiometricAvailable(context)) {
                        showBiometricPromptForSetup(
                            context as MainActivity,
                            KeyStore.initForEncryption(
                                KeyAlias.BiometricPrivateKey,
                                deviceAuthentication = true
                            ),
                            onAuthenticationSucceeded = { cipher ->
                                val encryptedData = KeyStore.encrypt(cipher, credentials.privateKey.fromHexString())

                                scope.launch {
                                    viewModel.credentialRepository.update(
                                        credentialsDb.copy(
                                            biometricEnabled = true,
                                            biometricPrivateKey = encryptedData.cipherText,
                                            biometricPrivateKeyIV = encryptedData.initializationVector
                                        )
                                    )
                                }

                                // navigate to dashboard
                                navController.navigate(
                                    screen = Screen.Vault,
                                    disableBack = true
                                )
                            },
                            onAuthenticationFailed = {
                                // navigate to dashboard
                                navController.navigate(
                                    screen = Screen.Vault,
                                    disableBack = true
                                )
                            }
                        )
                    } else {
                        // navigate to dashboard
                        navController.navigate(
                            screen = Screen.Vault,
                            disableBack = true
                        )
                    }
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
        keyboardType = KeyboardType.Email
    )

    Text(
        text = stringResource(R.string.GetPasswordHint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
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
                            e.showErrorToast(context)
                        }
                    }
                }
    )

    TextInputField(
        label = stringResource(R.string.Password),
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
                stringResource(R.string.Server_Official)
            }

            Server.TEST -> {
                stringResource(R.string.Server_Testing)
            }

            else -> server
        }
    }

    Row(
        modifier =
            Modifier
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
        enabled = email.isNotEmpty() && password.isNotEmpty(),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
    ) {
        Text(stringResource(R.string.Login))
    }

    var servers =
        listOf(Server.PRODUCTION)
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
        val text =
            when (it) {
                "custom_server" -> {
                    stringResource(R.string.Server_Choice_Dialog_AddCustom)
                }

                else -> getServerName(it)
            }

        Text(
            text = text,
            modifier =
                Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth()
        )
    }
}
