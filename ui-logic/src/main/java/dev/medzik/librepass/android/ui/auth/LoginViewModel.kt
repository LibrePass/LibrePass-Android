package dev.medzik.librepass.android.ui.auth

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.business.injection.VaultCacheModule
import dev.medzik.librepass.android.common.haveNetworkConnection
import dev.medzik.librepass.android.database.Credentials
import dev.medzik.librepass.android.database.injection.DatabaseProvider
import dev.medzik.librepass.android.ui.R
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.AuthClient
import dev.medzik.librepass.utils.fromHex

class LoginViewModel : ViewModel() {
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var server = mutableStateOf(Server.PRODUCTION)

    private val authClient = AuthClient(apiUrl = server.value)

    suspend fun login(context: Context) {
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

            val repository = DatabaseProvider.provideRepository(context)
            val vaultCache = VaultCacheModule.provideVaultCache(repository)

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

            vaultCache.aesKey = credentials.aesKey.fromHex()
        } catch (e: Exception) {
            // TODO
        }
    }
}
