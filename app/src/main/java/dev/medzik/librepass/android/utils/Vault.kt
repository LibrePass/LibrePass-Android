package dev.medzik.librepass.android.utils

import android.content.Context
import dev.medzik.android.crypto.DataStore.deleteEncrypted
import dev.medzik.android.crypto.DataStore.writeEncrypted
import dev.medzik.android.utils.runOnIOThread
import dev.medzik.librepass.android.data.CipherDao
import dev.medzik.librepass.android.data.LocalCipher
import dev.medzik.librepass.android.utils.SecretStore.AES_KEY_STORE_KEY
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.SecretStore.writeKey
import dev.medzik.librepass.types.api.SyncResponse
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import java.util.UUID

class Vault(
    private val cipherRepository: CipherDao
) {
    var aesKey: ByteArray = byteArrayOf()
    val ciphers = mutableListOf<Cipher>()

    fun decryptDatabase(ciphers: List<LocalCipher>) {
        ciphers.forEach {
            val cipher = Cipher(it.encryptedCipher, aesKey)
            this.ciphers.add(cipher)
        }
    }

    fun sync(syncResponse: SyncResponse) {
        val cacheCipherIDs: MutableList<UUID> = mutableListOf()
        ciphers.forEach { cacheCipherIDs.add(it.id) }

        // delete ciphers from the local database that are not in API response
        for (cipherId in cacheCipherIDs) {
            if (cipherId !in syncResponse.ids) {
                delete(cipherId)
            }
        }

        // update ciphers
        for (cipher in syncResponse.ciphers) {
            save(cipher, needUpload = false)
        }
    }

    fun sortAlphabetically(): List<Cipher> {
        return ciphers.sortedBy {
            when (it.type) {
                CipherType.Login -> {
                    it.loginData!!.name
                }
                CipherType.SecureNote -> {
                    it.secureNoteData!!.title
                }
                CipherType.Card -> {
                    it.cardData!!.name
                }
            }
        }
    }

    fun find(id: String): Cipher? = find(UUID.fromString(id))

    fun find(id: UUID): Cipher? = ciphers.find { it.id == id }

    fun filterByURI(uri: String): List<Cipher> = ciphers.filter { it.loginData?.uris?.contains(uri) == true }

    fun save(
        encryptedCipher: EncryptedCipher,
        needUpload: Boolean = true
    ) {
        return save(Cipher(encryptedCipher, aesKey), encryptedCipher, needUpload)
    }

    fun save(
        cipher: Cipher,
        encryptedCipher: EncryptedCipher? = null,
        needUpload: Boolean = true
    ) {
        ciphers.removeIf { it.id == cipher.id }
        ciphers.add(cipher)

        cipherRepository.insert(
            LocalCipher(encryptedCipher ?: EncryptedCipher(cipher, aesKey), needUpload)
        )
    }

    fun delete(id: UUID) {
        ciphers.removeIf { it.id == id }
        cipherRepository.delete(id)
    }

    fun saveVaultExpiration(context: Context) {
        val vaultTimeout = context.readKey(StoreKey.VaultTimeout)
        if (vaultTimeout == VaultTimeoutValues.INSTANT.seconds) {
            deleteSecrets(context)
        } else {
            runOnIOThread {
                context.dataStore.writeEncrypted(
                    KeyAlias.DataStoreEncrypted,
                    AES_KEY_STORE_KEY,
                    aesKey
                )
            }

            if (vaultTimeout != VaultTimeoutValues.INSTANT.seconds &&
                vaultTimeout != VaultTimeoutValues.NEVER.seconds
            ) {
                val currentTime = System.currentTimeMillis()
                val newExpiresTime = currentTime + (vaultTimeout * 1000)
                context.writeKey(StoreKey.VaultExpiresAt, newExpiresTime)
            }
        }
    }

    fun handleExpiration(context: Context): Boolean {
        val vaultTimeout = context.readKey(StoreKey.VaultTimeout)
        val expiresTime = context.readKey(StoreKey.VaultExpiresAt)
        val currentTime = System.currentTimeMillis()

        if (vaultTimeout == VaultTimeoutValues.NEVER.seconds)
            return false

        if (vaultTimeout == VaultTimeoutValues.INSTANT.seconds || currentTime > expiresTime) {
            deleteSecrets(context)

            return true
        }

        return false
    }

    fun deleteSecrets(context: Context) {
        aesKey = byteArrayOf()

        runOnIOThread {
            context.dataStore.deleteEncrypted(SecretStore.AES_KEY_STORE_KEY)
        }
    }
}
