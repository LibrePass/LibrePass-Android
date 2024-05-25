package dev.medzik.librepass.android.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import dev.medzik.librepass.android.R
import javax.crypto.Cipher

fun showBiometricPromptForUnlock(
    context: FragmentActivity,
    cipher: Cipher,
    onAuthenticationSucceeded: (Cipher) -> Unit,
    onAuthenticationFailed: () -> Unit
) {
    val promptInfo =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.BiometricUnlock_Title))
            .setSubtitle(context.getString(R.string.BiometricUnlock_Subtitle))
            .setNegativeButtonText(context.getString(R.string.BiometricUnlock_Button_UsePassword))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

    showBiometricPrompt(context, promptInfo, cipher, onAuthenticationSucceeded, onAuthenticationFailed)
}

fun showBiometricPromptForSetup(
    context: FragmentActivity,
    cipher: Cipher,
    onAuthenticationSucceeded: (Cipher) -> Unit,
    onAuthenticationFailed: () -> Unit
) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.BiometricSetup_Title))
        .setSubtitle(context.getString(R.string.BiometricSetup_Subtitle))
        .setNegativeButtonText(context.getString(R.string.BiometricSetup_Button_Cancel))
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()

    showBiometricPrompt(context, promptInfo, cipher, onAuthenticationSucceeded, onAuthenticationFailed)
}

private fun showBiometricPrompt(
    context: FragmentActivity,
    promptInfo: BiometricPrompt.PromptInfo,
    cipher: Cipher,
    onAuthenticationSucceeded: (Cipher) -> Unit,
    onAuthenticationFailed: () -> Unit
) {
    val biometricPrompt = BiometricPrompt(
        context,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) = onAuthenticationFailed()

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) = onAuthenticationSucceeded(result.cryptoObject?.cipher!!)

            override fun onAuthenticationFailed() = onAuthenticationFailed()
        }
    )

    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
}

fun checkIfBiometricAvailable(context: Context): Boolean {
    val status = BiometricManager.from(context)
        .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

    // return true when available
    return status == BiometricManager.BIOMETRIC_SUCCESS
}
