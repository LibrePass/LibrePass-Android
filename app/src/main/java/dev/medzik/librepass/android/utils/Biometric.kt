package dev.medzik.librepass.android.utils

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import dev.medzik.android.crypto.KeyStoreAlias
import dev.medzik.librepass.android.R
import javax.crypto.Cipher

enum class BiometricAlias : KeyStoreAlias {
    PrivateKey
}

object Biometric {
    fun showBiometricPrompt(
        context: FragmentActivity,
        cipher: Cipher,
        onAuthenticationSucceeded: (Cipher) -> Unit,
        onAuthenticationFailed: () -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.BiometricUnlock_Title))
            .setSubtitle(context.getString(R.string.BiometricUnlock_Subtitle))
            .setNegativeButtonText(context.getString(R.string.BiometricUnlock_Button_UsePassword))
            .build()

        val biometricPrompt = BiometricPrompt(
            context,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) =
                    onAuthenticationFailed()

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                    onAuthenticationSucceeded(result.cryptoObject?.cipher!!)

                override fun onAuthenticationFailed() =
                    onAuthenticationFailed()
            }
        )

        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}
