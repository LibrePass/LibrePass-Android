package dev.medzik.librepass.android.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.medzik.android.cryptoutils.DataStoreUtils.read
import dev.medzik.android.cryptoutils.DataStoreUtils.readEncrypted
import dev.medzik.android.cryptoutils.DataStoreUtils.write
import dev.medzik.android.cryptoutils.DataStoreUtils.writeEncrypted
import dev.medzik.librepass.android.UserSecretsStore
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "librepass")

class DataStoreUserSecrets(
    val privateKey: String,
    val secretKey: String
) {
    companion object {
        private const val PrivateKeyStoreKey = "private_key"
        private const val SecretKeyStoreKey = "secret_key"

        suspend fun init(context: Context): DataStoreUserSecrets {
            return DataStoreUserSecrets(
                privateKey = context.dataStore.readEncrypted(PrivateKeyStoreKey) ?: "",
                secretKey = context.dataStore.readEncrypted(SecretKeyStoreKey) ?: ""
            )
        }
    }

    suspend fun save(context: Context): DataStoreUserSecrets {
        context.dataStore.writeEncrypted(PrivateKeyStoreKey, privateKey)
        context.dataStore.writeEncrypted(SecretKeyStoreKey, secretKey)
        return this
    }
}

object DataStore {
    fun Context.getUserSecrets(): DataStoreUserSecrets? {
        return if (UserSecretsStore.privateKey.isBlank() || UserSecretsStore.secretKey.isBlank())
            null
        else UserSecretsStore
    }

    inline fun <reified T> Context.readKeyFromDataStore(key: DataStoreKey<T>): T {
        return runBlocking { dataStore.read(key.preferencesKey) } ?: key.default
    }

    inline fun <reified T> Context.writeKeyToDataStore(key: DataStoreKey<T>, value: T) {
        runBlocking { dataStore.write(key.preferencesKey, value) }
    }
}

sealed class DataStoreKey<T>(
    val preferencesKey: Preferences.Key<T>,
    val default: T
) {
    object DynamicColor : DataStoreKey<Boolean>(
        booleanPreferencesKey("dynamic_color"),
        true
    )

    object Theme : DataStoreKey<Int>(
        intPreferencesKey("theme"),
        0
    )

    object PasswordLength : DataStoreKey<Int>(
        intPreferencesKey("password_length"),
        15
    )

    object PasswordCapitalize : DataStoreKey<Boolean>(
        booleanPreferencesKey("password_capitalize"),
        true
    )

    object PasswordIncludeNumbers : DataStoreKey<Boolean>(
        booleanPreferencesKey("password_include_numbers"),
        true
    )

    object PasswordIncludeSymbols : DataStoreKey<Boolean>(
        booleanPreferencesKey("password_include_symbols"),
        true
    )
}
