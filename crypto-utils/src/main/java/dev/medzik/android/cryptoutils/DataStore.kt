package dev.medzik.android.cryptoutils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.medzik.libcrypto.AES
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object DataStore {
    /**
     * Read data from [DataStore].
     * @param preferenceKey Preference key to read
     * @return Value of preference or null if no preference
     */
    suspend inline fun <reified T> DataStore<Preferences>.read(preferenceKey: Preferences.Key<T>): T? {
        return data.map { it[preferenceKey] }.first()
    }

    /**
     * Write data to [DataStore].
     * @param preferenceKey Preference key to write
     * @param value Value to write
     */
    suspend inline fun <reified T> DataStore<Preferences>.write(
        preferenceKey: Preferences.Key<T>,
        value: T
    ) {
        edit { it[preferenceKey] = value }
    }

    /**
     * Delete data from [DataStore].
     * @param preferenceKey Preference key to delete
     */
    suspend inline fun <reified T> DataStore<Preferences>.delete(preferenceKey: Preferences.Key<T>) {
        edit { it.remove(preferenceKey) }
    }

    /**
     * Read encrypted data from [DataStore].
     * @param keyStoreAlias secret key alias in android keystore
     * @param preferenceKey preference key to read
     * @return Value of preference or null if no preference
     */
    suspend fun DataStore<Preferences>.readEncrypted(
        keyStoreAlias: String,
        preferenceKey: String
    ): String? {
        val cipherTextStore = stringPreferencesKey("$preferenceKey/encrypted")

        // read cipher text from datastore
        val cipherTextWithIV = read(cipherTextStore) ?: return null

        // get IV length in hex string
        val ivLength = AES.AesType.GCM.ivLength * 2

        // extract IV and Cipher Text from hex string
        val iv = cipherTextWithIV.substring(0, ivLength)
        val cipherText = cipherTextWithIV.substring(ivLength)

        // decrypt cipher text
        val cipher = KeyStore.initCipherForDecryption(iv, keyStoreAlias, false)
        return KeyStore.decrypt(cipher, cipherText)
    }

    /**
     * Write encrypted data to [DataStore].
     * @param keyStoreAlias secret key alias in android keystore
     * @param preferenceKey preference key to write
     * @param value Value to write
     */
    suspend fun DataStore<Preferences>.writeEncrypted(
        keyStoreAlias: String,
        preferenceKey: String,
        value: String
    ) {
        val cipherTextStore = stringPreferencesKey("$preferenceKey/encrypted")

        // encrypt value
        val cipher = KeyStore.initCipherForEncryption(keyStoreAlias, false)
        val cipherData = KeyStore.encrypt(cipher, value)

        // write encrypted value to datastore
        val cipherText = cipherData.initializationVector + cipherData.cipherText
        write(cipherTextStore, cipherText)
    }

    /**
     * Delete encrypted data from [DataStore].
     * @param preferenceKey preference key to delete
     */
    suspend fun DataStore<Preferences>.deleteEncrypted(preferenceKey: String) {
        val cipherTextStore = stringPreferencesKey("$preferenceKey/encrypted")
        delete(cipherTextStore)
    }
}
