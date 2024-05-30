package dev.medzik.librepass.android.utils

import android.content.Context
import dev.medzik.android.crypto.EncryptedDataStore.deleteEncryptedKey
import dev.medzik.android.crypto.EncryptedDataStore.readEncryptedKey
import dev.medzik.android.crypto.EncryptedDataStore.writeEncryptedKey
import dev.medzik.android.utils.runOnIOThread
import dev.medzik.librepass.android.database.LocalCipher
import dev.medzik.librepass.android.database.LocalCipherDao
import dev.medzik.librepass.android.database.datastore.VaultTimeoutValue
import dev.medzik.librepass.android.database.datastore.readVaultTimeout
import dev.medzik.librepass.android.database.datastore.writeVaultTimeout
import dev.medzik.librepass.android.utils.SecretStore.AES_KEY_STORE_KEY
import dev.medzik.librepass.types.api.SyncResponse
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import kotlinx.coroutines.runBlocking
import java.util.*

class Vault(
    private val cipherRepository: LocalCipherDao
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

    fun getVaultSecrets(context: Context) {
        val expired = handleExpiration(context)
        if (!expired) {
            runOnIOThread {
                aesKey = context.dataStore.readEncryptedKey(
                    KeyAlias.DataStoreEncrypted,
                    AES_KEY_STORE_KEY
                ) ?: byteArrayOf()
            }
        }
    }

    fun saveVaultExpiration(context: Context) {
        val vaultTimeout = runBlocking { readVaultTimeout(context) }

        if (vaultTimeout.timeout == VaultTimeoutValue.INSTANT) {
            deleteSecrets(context)
        } else {
            runOnIOThread {
                context.dataStore.writeEncryptedKey(
                    KeyAlias.DataStoreEncrypted,
                    AES_KEY_STORE_KEY,
                    aesKey
                )
            }

            if (vaultTimeout.timeout != VaultTimeoutValue.NEVER) {
                val currentTime = System.currentTimeMillis()
                val newExpiresTime = currentTime + (vaultTimeout.timeout.minutes * 60 * 1000)
                runOnIOThread { writeVaultTimeout(context, vaultTimeout.copy(expires = newExpiresTime)) }
            }
        }
    }

    fun handleExpiration(context: Context): Boolean {
        val vaultTimeout = runBlocking { readVaultTimeout(context) }
        val currentTime = System.currentTimeMillis()

        if (vaultTimeout.timeout == VaultTimeoutValue.NEVER)
            return false

        if (vaultTimeout.timeout == VaultTimeoutValue.INSTANT || currentTime > vaultTimeout.expires) {
            deleteSecrets(context)

            return true
        }

        return false
    }

    fun deleteSecrets(context: Context) {
        aesKey = byteArrayOf()

        runOnIOThread {
            context.dataStore.deleteEncryptedKey(AES_KEY_STORE_KEY)
        }
    }
}
