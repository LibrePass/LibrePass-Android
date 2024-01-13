package dev.medzik.librepass.android.utils

import dev.medzik.librepass.android.data.CipherDao
import dev.medzik.librepass.android.data.LocalCipher
import dev.medzik.librepass.types.api.SyncResponse
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import java.util.UUID

class Vault(
    private val cipherRepository: CipherDao
) {
    private lateinit var secretKey: ByteArray

    val ciphers = mutableListOf<Cipher>()

    fun decryptDatabase(
        secretKey: ByteArray,
        ciphers: List<LocalCipher>
    ) {
        this.secretKey = secretKey

        ciphers.forEach {
            val cipher = Cipher(it.encryptedCipher, secretKey)
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
        return save(Cipher(encryptedCipher, secretKey), encryptedCipher, needUpload)
    }

    fun save(
        cipher: Cipher,
        encryptedCipher: EncryptedCipher? = null,
        needUpload: Boolean = true
    ) {
        ciphers.removeIf { it.id == cipher.id }
        ciphers.add(cipher)

        cipherRepository.insert(
            LocalCipher(encryptedCipher ?: EncryptedCipher(cipher, secretKey), needUpload)
        )
    }

    fun delete(id: UUID) {
        ciphers.removeIf { it.id == id }
        cipherRepository.delete(id)
    }
}
