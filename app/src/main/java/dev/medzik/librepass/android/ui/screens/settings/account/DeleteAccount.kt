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
import dev.medzik.android.components.LoadingButton
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.components.rememberMutableString
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.components.TextInputField
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.UserClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun SettingsAccountDeleteAccountScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val credentials = viewModel.credentialRepository.get() ?: return

    var loading by rememberMutableBoolean()
    var password by rememberMutableString()
    val scope = rememberCoroutineScope()

    val userClient = UserClient(
        email = credentials.email,
        apiKey = credentials.apiKey,
        apiUrl = credentials.apiUrl ?: Server.PRODUCTION
    )

    fun deleteAccount(password: String) {
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                userClient.deleteAccount(password)

                runBlocking {
                    viewModel.credentialRepository.drop()
                    viewModel.cipherRepository.drop(credentials.userId)
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
        keyboardType = KeyboardType.Password
    )

    LoadingButton(
        loading = loading,
        onClick = { deleteAccount(password) },
        enabled = password.isNotEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 8.dp)
    ) {
        Text(stringResource(R.string.DeleteAccount))
    }
}
