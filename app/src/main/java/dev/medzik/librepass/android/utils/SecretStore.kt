package dev.medzik.librepass.android.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.medzik.android.cryptoutils.DataStore.deleteEncrypted
import dev.medzik.android.cryptoutils.DataStore.read
import dev.medzik.android.cryptoutils.DataStore.readEncrypted
import dev.medzik.android.cryptoutils.DataStore.write
import dev.medzik.android.cryptoutils.DataStore.writeEncrypted
import dev.medzik.librepass.android.UserSecretsStore
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "librepass")

object SecretStore {
    inline fun <reified T> Context.readKey(key: StoreKey<T>): T {
        return runBlocking { dataStore.read(key.preferenceKey) } ?: key.default
    }

    inline fun <reified T> Context.writeKey(key: StoreKey<T>, value: T) {
        runBlocking { dataStore.write(key.preferenceKey, value) }
    }

    fun initialize(context: Context): UserSecrets {
        val vaultTimeout = context.readKey(StoreKey.VaultTimeout)
        val expiresTime = context.readKey(StoreKey.VaultExpiresAt)
        val currentTime = System.currentTimeMillis()

        // check if the vault has expired
        if (vaultTimeout == VaultTimeoutValues.INSTANT.seconds ||
            (vaultTimeout != VaultTimeoutValues.NEVER.seconds && currentTime > expiresTime)
        ) return UserSecrets("", "")

        val userSecrets = suspend {
            UserSecrets(
                privateKey = context.dataStore.readEncrypted(
                    UserSecrets.KeyStoreAlias,
                    UserSecrets.PrivateKeyStoreKey
                ) ?: "",
                secretKey = context.dataStore.readEncrypted(
                    UserSecrets.KeyStoreAlias,
                    UserSecrets.SecretKeyStoreKey
                ) ?: ""
            )
        }

        return runBlocking { userSecrets() }
    }

    fun save(context: Context, userSecrets: UserSecrets): UserSecrets {
        val saveUserSecrets = suspend {
            context.dataStore.writeEncrypted(
                UserSecrets.KeyStoreAlias,
                UserSecrets.PrivateKeyStoreKey,
                userSecrets.privateKey
            )
            context.dataStore.writeEncrypted(
                UserSecrets.KeyStoreAlias,
                UserSecrets.SecretKeyStoreKey,
                userSecrets.secretKey
            )

            val vaultTimeout = context.readKey(StoreKey.VaultTimeout)
            if (vaultTimeout != VaultTimeoutValues.INSTANT.seconds &&
                vaultTimeout != VaultTimeoutValues.NEVER.seconds
            ) {
                val currentTime = System.currentTimeMillis()
                val newExpiresTime = currentTime + (vaultTimeout * 1000)
                context.writeKey(StoreKey.VaultExpiresAt, newExpiresTime)
            }

            userSecrets
        }

        return runBlocking { saveUserSecrets() }
    }

    fun delete(context: Context) {
        val clearUserSecrets = suspend {
            context.dataStore.deleteEncrypted(UserSecrets.PrivateKeyStoreKey)
            context.dataStore.deleteEncrypted(UserSecrets.SecretKeyStoreKey)
        }

        return runBlocking { clearUserSecrets() }
    }

    fun Context.getUserSecrets(): UserSecrets? {
        fun checkIfExists(userSecrets: UserSecrets): Boolean {
            return !(userSecrets.privateKey.isBlank() || userSecrets.secretKey.isBlank())
        }

        // check if secrets exist in UserSecretsStore
        if (checkIfExists(UserSecretsStore))
            return UserSecretsStore

        // check if secrets exist in DataStore
        val dataStoreUserSecrets = initialize(this)
        if (checkIfExists(dataStoreUserSecrets)) {
            // save secrets to in-memory global variable UserSecretsStore
            UserSecretsStore = dataStoreUserSecrets
            // return secrets from data store
            return dataStoreUserSecrets
        }

        // if secrets don't exist in UserSecretsStore and DataStore, return null
        return null
    }
}

data class UserSecrets(
    val privateKey: String,
    val secretKey: String
) {
    companion object {
        const val PrivateKeyStoreKey = "private_key"
        const val SecretKeyStoreKey = "secret_key"
        const val KeyStoreAlias = "librepass_datastore_secrets"
    }
}

sealed class StoreKey<T>(
    val preferenceKey: Preferences.Key<T>,
    val default: T
) {
    data object DynamicColor : StoreKey<Boolean>(
        booleanPreferencesKey("dynamic_color"),
        true
    )

    data object Theme : StoreKey<Int>(
        intPreferencesKey("theme"),
        ThemeValues.SYSTEM.ordinal
    )

    data object PasswordLength : StoreKey<Int>(
        intPreferencesKey("password_length"),
        15
    )

    data object PasswordCapitalize : StoreKey<Boolean>(
        booleanPreferencesKey("password_capitalize"),
        true
    )

    data object PasswordIncludeNumbers : StoreKey<Boolean>(
        booleanPreferencesKey("password_include_numbers"),
        true
    )

    data object PasswordIncludeSymbols : StoreKey<Boolean>(
        booleanPreferencesKey("password_include_symbols"),
        true
    )

    data object VaultTimeout : StoreKey<Int>(
        intPreferencesKey("vault_timeout"),
        VaultTimeoutValues.FIVE_MINUTES.seconds
    )

    data object VaultExpiresAt : StoreKey<Long>(
        longPreferencesKey("vault_expires_at"),
        System.currentTimeMillis()
    )
}

enum class ThemeValues {
    SYSTEM,
    LIGHT,
    DARK
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
            for (value in values())
                if (value.seconds == seconds)
                    return value

            throw IllegalArgumentException("No matching VaultTimeoutValues for seconds: $seconds")
        }
    }
}
