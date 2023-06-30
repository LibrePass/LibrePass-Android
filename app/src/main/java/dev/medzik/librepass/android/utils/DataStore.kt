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
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "librepass")

private const val DS_PRIVATE_KEY = "private_key"
private const val DS_SECRET_KEY = "secret_key"

suspend fun Context.getUserSecrets(): UserDataStoreSecrets? {
    val privateKey = dataStore.readEncrypted(DS_PRIVATE_KEY)
    val secretKey = dataStore.readEncrypted(DS_SECRET_KEY)

    return if (!privateKey.isNullOrBlank() && !secretKey.isNullOrBlank()) {
        UserDataStoreSecrets(
            privateKey = privateKey,
            secretKey = secretKey
        )
    } else null
}

fun Context.getUserSecretsSync() = runBlocking { getUserSecrets() }

suspend fun Context.writeUserSecrets(secrets: UserDataStoreSecrets) {
    dataStore.writeEncrypted(DS_PRIVATE_KEY, secrets.privateKey)
    dataStore.writeEncrypted(DS_SECRET_KEY, secrets.secretKey)
}

data class UserDataStoreSecrets(
    val privateKey: String,
    val secretKey: String
)

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

inline fun <reified T> Context.readKeyFromDataStore(key: DataStoreKey<T>): T {
    return runBlocking { dataStore.read(key.preferencesKey) } ?: key.default
}

inline fun <reified T> Context.writeKeyToDataStore(key: DataStoreKey<T>, value: T) {
    runBlocking { dataStore.write(key.preferencesKey, value) }
}
