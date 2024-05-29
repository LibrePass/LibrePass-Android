package dev.medzik.librepass.android.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.cipher.EncryptedCipher
import java.util.*

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
