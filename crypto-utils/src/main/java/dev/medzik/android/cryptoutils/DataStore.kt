package dev.medzik.android.cryptoutils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

object DataStoreUtils {
    /**
     * Reads the key from the data store.
     * @param key the key to read from the data store
     * @return key value or null if it does not exist
     */
    inline fun <reified T> DataStore<Preferences>.read(key: Preferences.Key<T>): T? {
        return runBlocking { data.map { it[key] }.first() }
    }

    /**
     * Writes the key to the data store.
     * @param key the key to write to the data store
     * @param value key value to write
     */
    inline fun <reified T> DataStore<Preferences>.write(key: Preferences.Key<T>, value: T) {
        runBlocking { edit { it[key] = value } }
    }

    fun DataStore<Preferences>.readEncrypted(storeKey: String): String? {
        val cipherTextStore = stringPreferencesKey("$storeKey/encrypted")
        val ivStore = stringPreferencesKey("$storeKey/iv")

        val cipherText = read(cipherTextStore) ?: return null
        val iv = read(ivStore) ?: return null

        val cipher = KeyStoreUtils.initCipherForDecryption(iv, "librepass_secrets", false)
        return KeyStoreUtils.decrypt(cipher, cipherText)
    }

    fun DataStore<Preferences>.writeEncrypted(storeKey: String, value: String) {
        val cipherTextStore = stringPreferencesKey("$storeKey/encrypted")
        val ivStore = stringPreferencesKey("$storeKey/iv")

        val cipher = KeyStoreUtils.initCipherForEncryption("librepass_secrets", false)
        val cipherData = KeyStoreUtils.encrypt(cipher, value)

        write(cipherTextStore, cipherData.cipherText)
        write(ivStore, cipherData.initializationVector)
    }
}
