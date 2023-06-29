package dev.medzik.android.cryptoutils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.apache.commons.codec.binary.Hex
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeyStoreUtils {
    /**
     * Initializes cipher for encryption.
     * @param alias secret key alias in key store
     * @param requireAuthentication if authentication is required for key store e.g. using biometric authentication
     * @return initialized cipher
     */
    fun initCipherForEncryption(
        alias: String,
        requireAuthentication: Boolean
    ): Cipher {
        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, getOrGenerateSecretKey(alias, requireAuthentication))
        return cipher
    }

    /**
     * Initializes cipher for decryption.
     * @param initializationVector AES initialization vector
     * @param alias secret key alias in key store
     * @param requireAuthentication if authentication is required for key store e.g. using biometric authentication
     * @return initialized cipher
     */
    fun initCipherForDecryption(
        initializationVector: String,
        alias: String,
        requireAuthentication: Boolean
    ): Cipher {
        val cipher = getCipher()
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrGenerateSecretKey(alias, requireAuthentication),
            GCMParameterSpec(128, Hex.decodeHex(initializationVector))
        )
        return cipher
    }

    /**
     * Encrypts data with specified cipher.
     * @param cipher cipher to use for encryption
     * @param data data to encrypt
     * @return cipher text and initialization vector
     */
    fun encrypt(cipher: Cipher, data: String): CipherText =
        CipherText(
            cipherText = Hex.encodeHexString(cipher.doFinal(data.toByteArray())),
            initializationVector = Hex.encodeHexString(cipher.iv)
        )

    /**
     * Decrypts data with specified cipher.
     * @param cipher cipher to use for decryption
     * @param cipherText cipher text to decrypt
     * @return decrypted data
     */
    fun decrypt(cipher: Cipher, cipherText: String): String =
        String(cipher.doFinal(Hex.decodeHex(cipherText)))

    private fun getCipher(): Cipher =
        Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/" +
                KeyProperties.BLOCK_MODE_GCM + "/" +
                KeyProperties.ENCRYPTION_PADDING_NONE
        )

    private fun getOrGenerateSecretKey(
        alias: String,
        requireAuthentication: Boolean
    ): SecretKey {
        return if (secretKeyExists(alias))
            getSecretKey(alias)
        else
            generateSecretKey(alias, requireAuthentication)
    }

    private fun secretKeyExists(alias: String): Boolean =
        getKeyStore().containsAlias(alias)

    private fun getSecretKey(alias: String): SecretKey =
        getKeyStore().getKey(alias, null) as SecretKey

    private fun generateSecretKey(alias: String, requireAuthentication: Boolean): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setUserAuthenticationRequired(requireAuthentication)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun getKeyStore(): KeyStore =
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    data class CipherText(
        val cipherText: String,
        val initializationVector: String
    )
}
