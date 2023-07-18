package dev.medzik.android.cryptoutils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dev.medzik.libcrypto.AES
import dev.medzik.libcrypto.EncryptException
import org.apache.commons.codec.binary.Hex
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android KeyStore utility class for encrypting and decrypting data.
 */
object KeyStore {
    /**
     * Initialize cipher for encryption.
     * @param alias secret key alias in android keystore
     * @param requireAuthentication true if authentication is required, for example, using biometric
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
     * Initialize cipher for decryption.
     * @param initializationVector AES initialization vector
     * @param alias secret key alias in key store
     * @param requireAuthentication true if authentication is required, for example, using biometric
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
            GCMParameterSpec(128, Hex.decodeHex(initializationVector.toCharArray()))
        )
        return cipher
    }

    /**
     * Encrypt data with specified cipher.
     * @param cipher cipher to use for encryption
     * @param data data to encrypt
     * @return cipher text and initialization vector
     */
    fun encrypt(cipher: Cipher, data: String): CipherText =
        CipherText(
            cipherText = String(Hex.encodeHex(cipher.doFinal(data.toByteArray()))),
            initializationVector = String(Hex.encodeHex(cipher.iv))
        )

    /**
     * Decrypt data with specified cipher.
     * @param cipher cipher to use for decryption
     * @param cipherText cipher text to decrypt
     * @return decrypted data
     */
    @Throws(EncryptException::class)
    fun decrypt(cipher: Cipher, cipherText: String): String {
        try {
            return String(cipher.doFinal(Hex.decodeHex(cipherText.toCharArray())))
        } catch (e: Exception) {
            throw EncryptException(e)
        }
    }

    /**
     * Delete keystore alias.
     * @param alias secret key alias in key store to delete
     */
    fun deleteAlias(alias: String) {
        val keyStore = getKeyStore()
        keyStore.deleteEntry(alias)
    }

    private fun getCipher(): Cipher =
        Cipher.getInstance(AES.AesType.GCM.mode)

    private fun getOrGenerateSecretKey(
        alias: String,
        requireAuthentication: Boolean
    ): SecretKey {
        return if (secretKeyExists(alias)) getSecretKey(alias)
        else generateSecretKey(alias, requireAuthentication)
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

    /**
     * Encrypted data.
     * @property cipherText cipher text
     * @property initializationVector initialization vector
     */
    data class CipherText(
        val cipherText: String,
        val initializationVector: String
    )
}
