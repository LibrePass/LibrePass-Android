package dev.medzik.librepass.android.business

import android.content.Context
import dev.medzik.android.utils.runOnIOThread
import dev.medzik.libcrypto.Hex
import dev.medzik.librepass.android.database.LocalCipher
import dev.medzik.librepass.android.database.LocalCipherDao
import dev.medzik.librepass.android.database.Repository
import dev.medzik.librepass.android.database.datastore.SecretsStore
import dev.medzik.librepass.android.database.datastore.VaultTimeoutValue
import dev.medzik.librepass.android.database.datastore.deleteSecretsStore
import dev.medzik.librepass.android.database.datastore.readSecretsStore
import dev.medzik.librepass.android.database.datastore.readVaultTimeout
import dev.medzik.librepass.android.database.datastore.writeSecretsStore
import dev.medzik.librepass.android.database.datastore.writeVaultTimeout
import dev.medzik.librepass.types.api.SyncResponse
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import kotlinx.coroutines.runBlocking
import java.util.UUID

class VaultCache(private val repository: Repository) {
    var aesKey: ByteArray = byteArrayOf()
    val ciphers = mutableListOf<Cipher>()

    fun decrypt(ciphers: List<LocalCipher>) {
        ciphers.forEach {
            val cipher = Cipher(it.encryptedCipher, aesKey)
            this.ciphers.add(cipher)
        }
    }

    fun getSecretsIfNotExpired(context: Context) {
        val expired = handleExpiration(context)
        if (!expired) {
            runOnIOThread {
                aesKey = Hex.decode(readSecretsStore(context).aesKey)
            }
        }
    }

    fun sync(response: SyncResponse) {
        val cacheCipherIDs: MutableList<UUID> = mutableListOf()
        ciphers.forEach { cacheCipherIDs.add(it.id) }

        // delete ciphers from the local database that are not in API response
        for (cipherId in cacheCipherIDs) {
            if (cipherId !in response.ids) {
                delete(cipherId)
            }
        }

        // update ciphers
        for (cipher in response.ciphers) {
            save(cipher, needUpload = false)
        }
    }

    fun find(id: UUID): Cipher? = ciphers.find { it.id == id }

    fun save(
        encryptedCipher: EncryptedCipher,
        needUpload: Boolean = true
    ) = save(
        cipher = Cipher(encryptedCipher, aesKey),
        encryptedCipher = encryptedCipher,
        needUpload = needUpload
    )

    fun save(
        cipher: Cipher,
        encryptedCipher: EncryptedCipher? = null,
        needUpload: Boolean = true
    ) {
        ciphers.removeIf { it.id == cipher.id }
        ciphers.add(cipher)

        repository.cipher.insert(
            LocalCipher(
                encryptedCipher = encryptedCipher ?: EncryptedCipher(cipher, aesKey),
                needUpload = needUpload
            )
        )
    }

    fun delete(id: UUID) {
        ciphers.removeIf { it.id == id }
        repository.cipher.delete(id)
    }

    fun getSortedCiphers(): List<Cipher> {
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

    fun saveVaultExpiration(context: Context) {
        val vaultTimeout = runBlocking { readVaultTimeout(context) }

        if (vaultTimeout.timeout == VaultTimeoutValue.INSTANT) {
            deleteSecrets(context)
        } else {
            runOnIOThread {
                writeSecretsStore(context, SecretsStore(Hex.encode(aesKey)))
            }

            if (vaultTimeout.timeout != VaultTimeoutValue.NEVER) {
                val currentTime = System.currentTimeMillis()
                val newExpiresTime = currentTime + (vaultTimeout.timeout.minutes * 60 * 1000)
                runOnIOThread {
                    writeVaultTimeout(context, vaultTimeout.copy(expires = newExpiresTime))
                }
            }
        }
    }

    fun deleteSecrets(context: Context) {
        aesKey = byteArrayOf()

        runOnIOThread {
            deleteSecretsStore(context)
        }
    }
}
