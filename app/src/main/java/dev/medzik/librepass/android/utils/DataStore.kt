package dev.medzik.librepass.android.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.medzik.android.cryptoutils.DataStoreUtils.read
import dev.medzik.android.cryptoutils.DataStoreUtils.readEncrypted
import dev.medzik.android.cryptoutils.DataStoreUtils.write
import dev.medzik.android.cryptoutils.DataStoreUtils.writeEncrypted
import dev.medzik.librepass.android.UserSecretsStore
import dev.medzik.librepass.android.utils.DataStore.readKeyFromDataStore
import dev.medzik.librepass.android.utils.DataStore.writeKeyToDataStore
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
            val expiresTime = context.readKeyFromDataStore(DataStoreKey.VaultExpiresAt)
            val currentTime = System.currentTimeMillis()
            val vaultTimeout = context.readKeyFromDataStore(DataStoreKey.VaultTimeout)

            // check if vault has expired
            if (vaultTimeout > 0 && currentTime > expiresTime)
                return DataStoreUserSecrets(privateKey = "", secretKey = "")

            // check if vault timeout is instant
            if (vaultTimeout == VaultTimeoutValues.INSTANT.seconds)
                return DataStoreUserSecrets(privateKey = "", secretKey = "")

            val newExpiresTime = currentTime + (vaultTimeout * 1000 * 1000)
            context.writeKeyToDataStore(DataStoreKey.VaultExpiresAt, newExpiresTime)

            return DataStoreUserSecrets(
                privateKey = context.dataStore.readEncrypted(PrivateKeyStoreKey) ?: "",
                secretKey = context.dataStore.readEncrypted(SecretKeyStoreKey) ?: ""
            )
        }
    }

    suspend fun save(context: Context): DataStoreUserSecrets {
        context.dataStore.writeEncrypted(PrivateKeyStoreKey, privateKey)
        context.dataStore.writeEncrypted(SecretKeyStoreKey, secretKey)
        val currentTime = System.currentTimeMillis()
        val vaultTimeout = context.readKeyFromDataStore(DataStoreKey.VaultTimeout)
        val newExpiresTime = currentTime + (vaultTimeout * 1000 * 1000)
        context.writeKeyToDataStore(DataStoreKey.VaultExpiresAt, newExpiresTime)
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

    object VaultTimeout : DataStoreKey<Int>(
        intPreferencesKey("vault_timeout"),
        VaultTimeoutValues.FIVE_MINUTES.seconds,
    )

    object VaultExpiresAt : DataStoreKey<Long>(
        longPreferencesKey("vault_expires_at"),
        System.currentTimeMillis(),
    )
}

enum class VaultTimeoutValues(val seconds: Int) {
    INSTANT(0),
    ONE_MINUTE(1 * 60),
    FIVE_MINUTES(5 * 60),
    FIFTEEN_MINUTES(15 * 60),
    THIRTY_MINUTES(30 * 60),
    ONE_HOUR(1 * 60 * 60),
    NEVER(-1);

    companion object {
        fun fromSeconds(seconds: Int): VaultTimeoutValues {
            for (value in values()) {
                if (value.seconds == seconds)
                    return value
            }

            throw IllegalArgumentException("No matching VaultTimeoutValues for seconds: $seconds")
        }
    }
}
