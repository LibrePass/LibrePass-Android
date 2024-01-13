package dev.medzik.librepass.android.utils

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dev.medzik.android.crypto.DataStore.read
import dev.medzik.android.crypto.DataStore.write
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore("librepass")

object SecretStore {
    const val AES_KEY_STORE_KEY = "aes_key"

    inline fun <reified T> Context.readKey(key: StoreKey<T>): T {
        return runBlocking { dataStore.read(key.preferenceKey) } ?: key.default
    }

    inline fun <reified T> Context.writeKey(
        key: StoreKey<T>,
        value: T
    ) {
        runBlocking { dataStore.write(key.preferenceKey, value) }
    }
}
