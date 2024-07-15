package dev.medzik.librepass.android.ui.auth

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.common.haveNetworkConnection
import dev.medzik.librepass.android.ui.R
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.AuthClient
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val retypePassword = mutableStateOf("")
    val passwordHint = mutableStateOf("")

    val retypePasswordError: Boolean
        get() = retypePassword.value != password.value
    val canSignup: Boolean
        get() = email.value.isNotEmpty() &&
                password.value.isNotEmpty() &&
                !retypePasswordError

    val server = mutableStateOf(Server.PRODUCTION)

    private val authClient = AuthClient(apiUrl = server.value)

    fun register(navController: NavController) {
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
            // TODO
        }
    }
}
