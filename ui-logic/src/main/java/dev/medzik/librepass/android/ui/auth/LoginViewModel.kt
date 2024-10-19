package dev.medzik.librepass.android.ui.auth

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.common.haveNetworkConnection
import dev.medzik.librepass.android.database.Credentials
import dev.medzik.librepass.android.database.Repository
import dev.medzik.librepass.android.ui.R
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.AuthClient
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: Repository
) : ViewModel() {
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var server = mutableStateOf(Server.PRODUCTION)

    private val authClient = AuthClient(apiUrl = server.value)

    suspend fun login() {
        if (!context.haveNetworkConnection()) {
            context.showToast(R.string.NoInternetConnection)
            return
        }

        try {
            val preLogin = authClient.preLogin(email.value)
            val credentials = authClient.login(
                email = email.value,
                password = password.value
            )

            val credentialsDbEntry = Credentials(
                userId = credentials.userId,
                email = email.value,
                apiUrl = if (server.value == Server.PRODUCTION) null else server.value,
                apiKey = credentials.apiKey,
                publicKey = credentials.publicKey,
                memory = preLogin.memory,
                iterations = preLogin.iterations,
                parallelism = preLogin.parallelism,
                biometricReSetup = true
            )
            repository.credentials.insert(credentialsDbEntry)

//            vaultCache.aesKey = credentials.aesKey.fromHex()
        } catch (e: Exception) {
            // TODO
        }
    }
}
