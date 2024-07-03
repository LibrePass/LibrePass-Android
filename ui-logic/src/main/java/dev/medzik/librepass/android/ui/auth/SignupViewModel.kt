package dev.medzik.librepass.android.ui.auth

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.common.haveNetworkConnection
import dev.medzik.librepass.android.ui.R
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.AuthClient

class SignupViewModel : ViewModel() {
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val retypePassword = mutableStateOf("")
    val retypePasswordIsValid = password.value.isNotEmpty() &&
            retypePassword.value == password.value
    val passwordHint = mutableStateOf("")
    val canLogin = email.value.isNotEmpty() &&
            password.value.isNotEmpty() &&
            retypePassword.value == password.value

    val server = mutableStateOf(Server.PRODUCTION)

    private val authClient = AuthClient(apiUrl = server.value)

    fun register(context: Context, navController: NavController) {
        if (!context.haveNetworkConnection()) {
            context.showToast(R.string.NoInternetConnection)
            return
        }

        try {
            authClient.register(
                email = email.value,
                password = password.value
            )

            // TODO: disable back
            navController.navigate(Login)
        } catch (e: Exception) {

        }
    }
}
