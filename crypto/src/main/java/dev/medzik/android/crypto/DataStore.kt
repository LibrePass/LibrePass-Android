package dev.medzik.android.crypto

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.medzik.libcrypto.Hex
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Android DataStore utility class for storing data */
object DataStore {
    /**
     * Reads the key from KeyStore.
     * @param preferenceKey key to read from KeyStore
     * @return the value of the key
     */
    suspend inline fun <reified T> DataStore<Preferences>.read(preferenceKey: Preferences.Key<T>): T? {
        return data.map { it[preferenceKey] }.first()
    }

    /**
     * Writes the key to KeyStore.
     * @param preferenceKey key to write to KeyStore
     * @param value the value to write
     */
    suspend inline fun <reified T> DataStore<Preferences>.write(
        preferenceKey: Preferences.Key<T>,
        value: T
    ) {
        edit { it[preferenceKey] = value }
    }

    /**
     * Deletes the key from KeyStore.
     * @param preferenceKey key to delete from KeyStore
     */
    suspend inline fun <reified T> DataStore<Preferences>.delete(preferenceKey: Preferences.Key<T>) {
        edit { it.remove(preferenceKey) }
    }

    /**
     * Reads the encrypted key from KeyStore.
     * @param keyStoreAlias secret key alias in Android KeyStore to decrypt the value
     * @param preferenceKey key to read from KeyStore
     * @return the decrypted value of the key
     */
    suspend fun DataStore<Preferences>.readEncrypted(
        keyStoreAlias: KeyStoreAlias,
        preferenceKey: String
    ): ByteArray? {
        val cipherTextStore = stringPreferencesKey("$preferenceKey/encrypted")

        // read cipher text from datastore
        val cipherTextWithIV = read(cipherTextStore) ?: return null

        // initialization vector length in hex string
        val ivLength = 12 * 2

        // extract IV and Cipher Text from hex string
        val iv = cipherTextWithIV.substring(0, ivLength)
        val cipherText = cipherTextWithIV.substring(ivLength)

        // decrypt cipher text
        val cipher = KeyStore.initForDecryption(Hex.decode(iv), keyStoreAlias, false)
        return KeyStore.decrypt(cipher, cipherText)
    }

    /**
     * Writes the encrypted key from KeyStore.
     * @param keyStoreAlias secret key alias in Android KeyStore to encrypt the value
     * @param preferenceKey key to write KeyStore
     * @param value value to encrypt and write
     * @return the value of the key
     */
    suspend fun DataStore<Preferences>.writeEncrypted(
        keyStoreAlias: KeyStoreAlias,
        preferenceKey: String,
        value: ByteArray
    ) {
        val cipherTextStore = stringPreferencesKey("$preferenceKey/encrypted")

        // encrypt value
        val cipher = KeyStore.initForEncryption(keyStoreAlias, false)
        val cipherData = KeyStore.encrypt(cipher, value)

        // write encrypted value to datastore
        val cipherText = cipherData.initializationVector + cipherData.cipherText
        write(cipherTextStore, cipherText)
    }

    /**
     * Deletes the encrypted key from KeyStore.
     * @param preferenceKey key to delete from KeyStore
     */
    suspend fun DataStore<Preferences>.deleteEncrypted(preferenceKey: String) {
        val cipherTextStore = stringPreferencesKey("$preferenceKey/encrypted")
        delete(cipherTextStore)
    }
}
