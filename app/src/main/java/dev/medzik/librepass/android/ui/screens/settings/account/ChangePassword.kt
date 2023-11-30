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
import dev.medzik.librepass.client.utils.Cryptography.computeSecretKeyFromPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun SettingsAccountChangePasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = context.getRepository()
    val credentials = repository.credentials.get() ?: return
    val userSecrets = context.getUserSecrets() ?: return

    var oldPassword by rememberMutableString()
    var oldPasswordInvalid by rememberMutableBoolean()
    var newPassword by rememberMutableString()
    var newPasswordConfirm by rememberMutableString()
    var newPasswordHint by rememberMutableString()
    var loading by rememberMutableBoolean()

    val scope = rememberCoroutineScope()

    val userClient =
        UserClient(
            email = credentials.email,
            apiKey = credentials.apiKey,
            apiUrl = credentials.apiUrl ?: Server.PRODUCTION
        )

    fun resetPassword(
        oldPassword: String,
        newPassword: String,
        newPasswordHint: String
    ) {
        loading = true
        oldPasswordInvalid = false

        scope.launch(Dispatchers.IO) {
            // check old password
            // compute base password hash
            val oldPasswordHash =
                computeSecretKeyFromPassword(
                    password = oldPassword,
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
                oldPasswordInvalid = true
                loading = false
                return@launch
            }

            try {
                userClient.changePassword(oldPassword, newPassword, newPasswordHint)

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
        label = stringResource(R.string.Settings_ChangePassword_OldPassword),
        value = oldPassword,
        onValueChange = { oldPassword = it },
        hidden = true,
        isError = oldPasswordInvalid,
        errorMessage = stringResource(R.string.Settings_ChangePassword_Error_InvalidOldPassword),
        keyboardType = KeyboardType.Password
    )

    TextInputField(
        label = stringResource(R.string.Settings_ChangePassword_NewPassword),
        value = newPassword,
        onValueChange = { newPassword = it },
        hidden = true,
        isError = newPassword.isNotEmpty() && newPassword.length < 8,
        errorMessage = stringResource(R.string.PasswordTooShort),
        keyboardType = KeyboardType.Password
    )

    TextInputField(
        label = stringResource(R.string.Settings_ChangePassword_ConfirmNewPassword),
        value = newPasswordConfirm,
        onValueChange = { newPasswordConfirm = it },
        hidden = true,
        isError = newPasswordConfirm.isNotEmpty() && newPasswordConfirm != newPassword,
        errorMessage = stringResource(R.string.PasswordsDoNotMatch),
        keyboardType = KeyboardType.Password
    )

    TextInputField(
        label = stringResource(R.string.InputField_PasswordHint),
        value = newPasswordHint,
        onValueChange = { newPasswordHint = it }
    )

    LoadingButton(
        loading = loading,
        onClick = { resetPassword(oldPassword, newPassword, newPasswordHint) },
        enabled = oldPassword.isNotEmpty() && newPassword.isNotEmpty() && newPasswordConfirm == newPassword,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 8.dp)
    ) {
        Text(stringResource(R.string.Settings_ChangePassword_Button))
    }
}
