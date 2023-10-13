package dev.medzik.librepass.android.utils

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dev.medzik.android.crypto.DataStore.deleteEncrypted
import dev.medzik.android.crypto.DataStore.read
import dev.medzik.android.crypto.DataStore.readEncrypted
import dev.medzik.android.crypto.DataStore.write
import dev.medzik.android.crypto.DataStore.writeEncrypted
import dev.medzik.android.crypto.KeyStoreAlias
import dev.medzik.libcrypto.Hex
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
                privateKey = Hex.encode(
                    context.dataStore.readEncrypted(
                        KeyStore.DataStoreEncrypted,
                        UserSecrets.PrivateKeyStoreKey
                    ) ?: ByteArray(0)
                ),
                secretKey = Hex.encode(
                    context.dataStore.readEncrypted(
                        KeyStore.DataStoreEncrypted,
                        UserSecrets.SecretKeyStoreKey
                    ) ?: ByteArray(0)
                )
            )
        }

        return runBlocking { userSecrets() }
    }

    fun save(context: Context, userSecrets: UserSecrets): UserSecrets {
        val saveUserSecrets = suspend {
            context.dataStore.writeEncrypted(
                KeyStore.DataStoreEncrypted,
                UserSecrets.PrivateKeyStoreKey,
                Hex.decode(userSecrets.privateKey)
            )
            context.dataStore.writeEncrypted(
                KeyStore.DataStoreEncrypted,
                UserSecrets.SecretKeyStoreKey,
                Hex.decode(userSecrets.secretKey)
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
    }
}

enum class KeyStore : KeyStoreAlias {
    DataStoreEncrypted,
}
