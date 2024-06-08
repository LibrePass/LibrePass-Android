package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
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
import dev.medzik.android.components.TextFieldValue
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.components.rememberMutableString
import dev.medzik.android.components.ui.LoadingButton
import dev.medzik.android.components.ui.textfield.AnimatedTextField
import dev.medzik.android.components.ui.textfield.PasswordAnimatedTextField
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.common.haveNetworkConnection
import dev.medzik.librepass.android.common.popUpToStartDestination
import dev.medzik.librepass.android.database.Credentials
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.components.auth.ChoiceServer
import dev.medzik.librepass.android.ui.screens.vault.Vault
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.AuthClient
import dev.medzik.librepass.utils.fromHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object Login

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var loading by rememberMutableBoolean()
    val email = rememberMutableString()
    val password = rememberMutableString()
    val server = rememberMutableString(Server.PRODUCTION)

    fun submit(email: String, password: String) {
        if (!context.haveNetworkConnection()) {
            context.showToast(R.string.Error_NoInternetConnection)
            return
        }

        val authClient = AuthClient(apiUrl = server.value)

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

                // save credentials
                val credentialsDb = Credentials(
                    userId = credentials.userId,
                    email = email,
                    apiUrl = if (server.value == Server.PRODUCTION) null else server.value,
                    apiKey = credentials.apiKey,
                    publicKey = credentials.publicKey,
                    // Argon2id parameters
                    memory = preLogin.memory,
                    iterations = preLogin.iterations,
                    parallelism = preLogin.parallelism
                )
                viewModel.credentialRepository.insert(credentialsDb)

                viewModel.vault.aesKey = credentials.aesKey.fromHex()

                viewModel.credentialRepository.update(
                    credentialsDb.copy(
                        biometricReSetup = true
                    )
                )

                runOnUiThread {
                    navController.navigate(Vault) {
                        popUpToStartDestination(navController)
                    }
                }
            } catch (e: Exception) {
                loading = false
                e.showErrorToast(context)
            }
        }
    }

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

    fun requestPasswordHint() {
        val authClient = AuthClient(apiUrl = server.value)

        if (email.value.isEmpty()) {
            context.showToast(context.getString(R.string.Toast_Enter_Email))
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                authClient.requestPasswordHint(email.value)

                context.showToast(context.getString(R.string.Toast_Password_Hint_Sent))
            } catch (e: Exception) {
                e.showErrorToast(context)
            }
        }
    }

    Text(
        text = stringResource(R.string.GetPasswordHint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable { requestPasswordHint() }
    )

    PasswordAnimatedTextField(
        label = stringResource(R.string.Password),
        value = TextFieldValue.fromMutableState(password),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        )
    )

    ChoiceServer(navController, server)

    LoadingButton(
        loading = loading,
        onClick = { submit(email.value, password.value) },
        enabled = email.value.isNotEmpty() && password.value.isNotEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
    ) {
        Text(stringResource(R.string.Login))
    }
}
