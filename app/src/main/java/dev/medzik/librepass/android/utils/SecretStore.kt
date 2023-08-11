package dev.medzik.librepass.android.utils

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dev.medzik.android.cryptoutils.DataStore.deleteEncrypted
import dev.medzik.android.cryptoutils.DataStore.read
import dev.medzik.android.cryptoutils.DataStore.readEncrypted
import dev.medzik.android.cryptoutils.DataStore.write
import dev.medzik.android.cryptoutils.DataStore.writeEncrypted
import dev.medzik.librepass.android.MainActivity
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

        // save changes in-memory variable
        (context as MainActivity).userSecrets = userSecrets

        return runBlocking { saveUserSecrets() }
    }

    fun delete(context: Context) {
        val clearUserSecrets = suspend {
            context.dataStore.deleteEncrypted(UserSecrets.PrivateKeyStoreKey)
            context.dataStore.deleteEncrypted(UserSecrets.SecretKeyStoreKey)
        }

        // clear data from in-memory variable
        (context as MainActivity).userSecrets = UserSecrets("", "")

        return runBlocking { clearUserSecrets() }
    }

    fun Context.getUserSecrets(): UserSecrets? {
        // get secrets from in-memory variable
        val userSecrets = (this as MainActivity).userSecrets

        if (userSecrets.privateKey.isBlank() || userSecrets.secretKey.isBlank())
            return null

        return userSecrets
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
