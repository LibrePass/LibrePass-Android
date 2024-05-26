package dev.medzik.librepass.android.ui.screens.auth

import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.components.LoadingButton
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.components.rememberMutableString
import dev.medzik.android.crypto.KeyStore
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.android.utils.showToast
import dev.medzik.libcrypto.Argon2
import dev.medzik.libcrypto.Hex
import dev.medzik.libcrypto.X25519
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.components.TextInputField
import dev.medzik.librepass.android.ui.screens.vault.Vault
import dev.medzik.librepass.android.utils.*
import dev.medzik.librepass.utils.Cryptography.computeAesKey
import dev.medzik.librepass.utils.Cryptography.computePasswordHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
object Unlock

@Composable
fun UnlockScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    // context must be FragmentActivity to show biometric prompt
    val context = LocalContext.current as FragmentActivity

    val scope = rememberCoroutineScope()

    var loading by rememberMutableBoolean()
    var password by rememberMutableString()

    val credentials = viewModel.credentialRepository.get() ?: return

    fun onUnlock(password: String) {
        // disable button
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                loading = true

                // compute base password hash
                val passwordHash = computePasswordHash(
                    password = password,
                    email = credentials.email,
                    argon2Function = Argon2(
                        32,
                        credentials.parallelism,
                        credentials.memory,
                        credentials.iterations
                    )
                )

                val publicKey = X25519.publicFromPrivate(passwordHash.hash)

                if (Hex.encode(publicKey) != credentials.publicKey)
                    throw Exception("Invalid password")

                viewModel.vault.aesKey = computeAesKey(passwordHash.hash)

                // run only if loading is true (if no error occurred)
                if (loading) {
                    runOnUiThread {
                        navController.navigate(
                            Vault
                        ) {
                            popUpToDestination(navController)
                        }
                    }
                }
            } catch (e: Exception) {
                // if password is invalid
                loading = false
                context.showToast(R.string.Error_InvalidCredentials)
            }
        }
    }

    fun showBiometric() {
        try {
            showBiometricPromptForUnlock(
                context,
                KeyStore.initForDecryption(
                    alias = KeyAlias.BiometricAesKey,
                    initializationVector = Hex.decode(credentials.biometricAesKeyIV!!),
                    deviceAuthentication = false
                ),
                onAuthenticationSucceeded = { cipher ->
                    viewModel.vault.aesKey = KeyStore.decrypt(cipher, credentials.biometricAesKey!!)

                    navController.navigate(
                        Vault
                    ) {
                        popUpToDestination(navController)
                    }
                },
                onAuthenticationFailed = { }
            )
        } catch (e: KeyPermanentlyInvalidatedException) {
            // after adding or removing fingerprint, the key is invalidated
            context.showToast(R.string.BiometricKeyInvalidated)

            try {
                KeyStore.deleteKey(KeyAlias.BiometricAesKey.name)
                runBlocking {
                    viewModel.credentialRepository.update(
                        credentials.copy(
                            biometricReSetup = true,
                            biometricAesKey = null,
                            biometricAesKeyIV = null
                        )
                    )
                }
            } catch (e: Exception) {
                e.debugLog()
            }
        } catch (e: Exception) {
            e.showErrorToast(context)
        }
    }

    LaunchedEffect(scope) {
        if (credentials.biometricAesKey != null && checkIfBiometricAvailable(context))
            showBiometric()
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
        onClick = { onUnlock(password) },
        enabled = password.isNotEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .padding(horizontal = 80.dp)
    ) {
        Text(stringResource(R.string.Unlock))
    }

    if (credentials.biometricAesKey != null && checkIfBiometricAvailable(context)) {
        OutlinedButton(
            onClick = { showBiometric() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(horizontal = 80.dp)
        ) {
            Text(stringResource(R.string.UseBiometric))
        }
    }
}
