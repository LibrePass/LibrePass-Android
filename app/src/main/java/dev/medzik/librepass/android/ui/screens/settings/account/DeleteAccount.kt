package dev.medzik.librepass.android.ui.screens.settings.account

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
import dev.medzik.libcrypto.Argon2
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.components.TextInputField
import dev.medzik.librepass.android.utils.SecretStore.getUserSecrets
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.UserClient
import dev.medzik.librepass.client.utils.Cryptography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun SettingsAccountDeleteAccountScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = context.getRepository()
    val credentials = repository.credentials.get() ?: return
    val userSecrets = context.getUserSecrets() ?: return

    var loading by rememberMutableBoolean()
    var password by rememberMutableString()
    var passwordInvalid by rememberMutableBoolean()
    val scope = rememberCoroutineScope()

    val userClient =
        UserClient(
            email = credentials.email,
            apiKey = credentials.apiKey,
            apiUrl = credentials.apiUrl ?: Server.PRODUCTION
        )

    fun deleteAccount(password: String) {
        loading = true
        passwordInvalid = false

        scope.launch(Dispatchers.IO) {
            // check old password
            // compute base password hash
            val oldPasswordHash =
                Cryptography.computeSecretKeyFromPassword(
                    password = password,
                    email = credentials.email,
                    argon2Function =
                        Argon2(
                            32,
                            credentials.parallelism,
                            credentials.memory,
                            credentials.iterations
                        )
                )

            if (!oldPasswordHash.contentEquals(userSecrets.secretKey)) {
                passwordInvalid = true
                loading = false
                return@launch
            }

            try {
                userClient.deleteAccount(password)

                runBlocking {
                    repository.credentials.drop()
                    repository.cipher.drop(credentials.userId)
                    context
                }

                runOnUiThread {
                    navController.navigate(
                        screen = Screen.Welcome,
                        disableBack = true
                    )
                }
            } catch (e: Exception) {
                e.showErrorToast(context)

                loading = false
            }
        }
    }

    TextInputField(
        label = stringResource(R.string.Password),
        value = password,
        onValueChange = { password = it },
        hidden = true,
        isError = passwordInvalid,
        errorMessage = stringResource(R.string.Error_InvalidPassword),
        keyboardType = KeyboardType.Password
    )

    LoadingButton(
        loading = loading,
        onClick = { deleteAccount(password) },
        enabled = password.isNotEmpty(),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 8.dp)
    ) {
        Text(stringResource(R.string.DeleteAccount))
    }
}
