package dev.medzik.librepass.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import dev.medzik.librepass.types.api.EncryptedCipher
import kotlinx.serialization.json.Json
import java.util.UUID

@Entity
class CipherTable(
    @PrimaryKey
    val id: UUID,
    val owner: UUID,
    @field:TypeConverters(EncryptedCipherConverter::class)
    var encryptedCipher: EncryptedCipher,
)

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
