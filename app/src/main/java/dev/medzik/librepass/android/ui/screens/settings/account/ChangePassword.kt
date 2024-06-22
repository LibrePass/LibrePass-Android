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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.compose.rememberMutable
import dev.medzik.android.compose.ui.LoadingButton
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.common.LibrePassViewModel
import dev.medzik.librepass.android.common.haveNetworkConnection
import dev.medzik.librepass.android.ui.components.TextInputField
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.UserClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object SettingsAccountChangePassword

@Composable
fun SettingsAccountChangePasswordScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val credentials = viewModel.credentialRepository.get() ?: return

    var oldPassword by rememberMutable("")
    var newPassword by rememberMutable("")
    var newPasswordConfirm by rememberMutable("")
    var newPasswordHint by rememberMutable("")
    var loading by rememberMutable(false)

    val scope = rememberCoroutineScope()

    val userClient = UserClient(
        email = credentials.email,
        apiKey = credentials.apiKey,
        apiUrl = credentials.apiUrl ?: Server.PRODUCTION
    )

    fun changePassword(oldPassword: String, newPassword: String, newPasswordHint: String) {
        if (!context.haveNetworkConnection()) {
            context.showToast(R.string.Error_NoInternetConnection)
            return
        }

        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                userClient.changePassword(oldPassword, newPassword, newPasswordHint)

                navigateToWelcomeAndLogout(viewModel, navController, credentials.userId)
            } catch (e: Exception) {
                e.showErrorToast(context)

                loading = false
            }
        }
    }

    TextInputField(
        label = stringResource(R.string.OldPassword),
        value = oldPassword,
        onValueChange = { oldPassword = it },
        hidden = true,
        emptySupportingText = true,
        keyboardType = KeyboardType.Password
    )

    TextInputField(
        label = stringResource(R.string.NewPassword),
        value = newPassword,
        onValueChange = { newPassword = it },
        hidden = true,
        isError = newPassword.isNotEmpty() && newPassword.length < 8,
        errorMessage = stringResource(R.string.Error_PasswordTooShort),
        keyboardType = KeyboardType.Password
    )

    TextInputField(
        label = stringResource(R.string.ConfirmNewPassword),
        value = newPasswordConfirm,
        onValueChange = { newPasswordConfirm = it },
        hidden = true,
        isError = newPasswordConfirm.isNotEmpty() && newPasswordConfirm != newPassword,
        errorMessage = stringResource(R.string.Error_PasswordsDoNotMatch),
        keyboardType = KeyboardType.Password
    )

    TextInputField(
        label = stringResource(R.string.PasswordHint),
        value = newPasswordHint,
        onValueChange = { newPasswordHint = it },
        emptySupportingText = true
    )

    LoadingButton(
        loading = loading,
        onClick = { changePassword(oldPassword, newPassword, newPasswordHint) },
        enabled = oldPassword.isNotEmpty() && newPassword.isNotEmpty() && newPasswordConfirm == newPassword,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 8.dp)
    ) {
        Text(stringResource(R.string.ChangePassword))
    }
}
