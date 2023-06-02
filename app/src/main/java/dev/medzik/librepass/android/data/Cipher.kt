package dev.medzik.librepass.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import dev.medzik.librepass.types.cipher.EncryptedCipher
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * CipherTable is a table that stores encrypted ciphers.
 * It is such as a cache to avoid pointlessly downloading the same data from the server and working offline.
 * @param id The id of the cipher.
 * @param owner The id of the owner of the cipher.
 * @param encryptedCipher The encrypted cipher. It is encrypted with the user's encryption key. It is stored as a JSON string.
 */
@Entity
class CipherTable(
    @PrimaryKey
    val id: UUID,
    val owner: UUID,
    @field:TypeConverters(EncryptedCipherConverter::class)
    var encryptedCipher: EncryptedCipher
)

/**
 * EncryptedCipherConverter is a converter that converts an EncryptedCipher to a JSON string and vice versa.
 */
class EncryptedCipherConverter {
    @TypeConverter
    fun fromEncryptedCipher(encryptedCipher: EncryptedCipher): String {
        return encryptedCipher.toJson()
    }

    @TypeConverter
    fun toEncryptedCipher(json: String): EncryptedCipher {
        return Json.decodeFromString(EncryptedCipher.serializer(), json)
    }
}
