package dev.medzik.librepass.android.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.apache.commons.codec.binary.Hex
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * KeyStore cipher text.
 * @param cipherText The cipher text.
 * @param initializationVector The initialization vector (IV) for this cipher text.
 */
data class KeyStoreCipherText(val cipherText: String, val initializationVector: String)

/**
 * Utilities for Android Key Store. It is used for biometric authentication.
 */
object KeyStoreUtils {
    /**
     * Get Cipher for encryption.
     * @param alias Alias for secret key.
     */
    fun getCipherForEncryption(alias: String): Cipher {
        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey(alias))
        return cipher
    }

    /**
     * Get Cipher for decryption.
     * @param alias Alias for secret key.
     * @param initializationVector Initialization vector.
     */
    fun getCipherForDecryption(
        alias: String,
        initializationVector: String
    ): Cipher {
        val cipher = getCipher()
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(alias),
            IvParameterSpec(Hex.decodeHex(initializationVector))
        )
        return cipher
    }

    /**
     * Encrypt data with given cipher. Returns cipher text and initialization vector.
     * @param cipher Cipher for encryption.
     * @param data Data to encrypt.
     * @return Cipher text and initialization vector.
     */
    fun encrypt(cipher: Cipher, data: String): KeyStoreCipherText {
        val cipherText = cipher.doFinal(data.toByteArray())
        return KeyStoreCipherText(Hex.encodeHexString(cipherText), Hex.encodeHexString(cipher.iv))
    }

    /**
     * Decrypt data with given cipher. Returns plain text.
     * @param cipher Cipher for decryption.
     * @param data Data to decrypt.
     */
    fun decrypt(cipher: Cipher, data: String): String {
        return String(cipher.doFinal(Hex.decodeHex(data)))
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/" +
                KeyProperties.BLOCK_MODE_CBC + "/" +
                KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }

    private fun getOrCreateSecretKey(alias: String): SecretKey {
        return if (isSecretKeyExist(alias)) {
            getSecretKey(alias)
        } else {
            generateSecretKey(alias)
        }
    }

    private fun isSecretKeyExist(alias: String): Boolean {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        return keyStore.containsAlias(alias)
    }

    private fun getSecretKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        return keyStore.getKey(alias, null) as SecretKey
    }

    private fun generateSecretKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            // Require the user to authenticate with a biometric to authorize every use of the key
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
}

/**
 * Alias for secrets in KeyStore.
 */
enum class KeyStoreAlias {
    PRIVATE_KEY
}
