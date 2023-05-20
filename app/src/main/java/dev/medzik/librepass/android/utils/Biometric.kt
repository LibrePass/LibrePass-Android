package dev.medzik.librepass.android.utils

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import dev.medzik.librepass.android.R
import javax.crypto.Cipher

/**
 * Show biometric prompt and call [onAuthenticationSucceeded] if authentication succeeded or
 * [onAuthenticationFailed] if authentication failed.
 * @param context [FragmentActivity] context
 * @param cipher [Cipher] to use for authentication (should be initialized) (see [KeyStoreUtils])
 * @param onAuthenticationSucceeded callback called when authentication succeeded
 * @param onAuthenticationFailed callback called when authentication failed
 * @see BiometricPrompt
 */
fun showBiometricPrompt(
    context: FragmentActivity,
    cipher: Cipher,
    onAuthenticationSucceeded: (Cipher) -> Unit,
    onAuthenticationFailed: () -> Unit
) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.biometric_title))
        .setSubtitle(context.getString(R.string.biometric_subtitle))
        .setNegativeButtonText(context.getString(R.string.cancel))
        .build()

    val biometricPrompt = BiometricPrompt(
        context,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                onAuthenticationFailed()
            }

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                onAuthenticationSucceeded(result.cryptoObject?.cipher!!)
            }

            override fun onAuthenticationFailed() {
                onAuthenticationFailed()
            }
        }
    )

    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
}
