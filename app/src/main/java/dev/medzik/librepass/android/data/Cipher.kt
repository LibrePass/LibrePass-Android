package dev.medzik.librepass.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.cipher.EncryptedCipher
import java.util.UUID

/**
 * LocalCipher is a table that stores encrypted ciphers.
 * It is such as a cache to avoid pointlessly downloading the same data from the server and working offline.
 */
@Entity
class LocalCipher(
    @PrimaryKey
    val id: UUID,
    val owner: UUID,
    val needUpload: Boolean,
    @field:TypeConverters(EncryptedCipherConverter::class)
    var encryptedCipher: EncryptedCipher
) {
    constructor(
        encryptedCipher: EncryptedCipher,
        needUpload: Boolean = false
    ) : this(
        id = encryptedCipher.id,
        owner = encryptedCipher.owner,
        needUpload = needUpload,
        encryptedCipher = encryptedCipher
    )
}

class EncryptedCipherConverter {
    @TypeConverter
    fun fromEncryptedCipher(encryptedCipher: EncryptedCipher): String {
        return JsonUtils.serialize(encryptedCipher)
    }

    @TypeConverter
    fun toEncryptedCipher(json: String): EncryptedCipher {
        return JsonUtils.deserialize(json)
    }
}
