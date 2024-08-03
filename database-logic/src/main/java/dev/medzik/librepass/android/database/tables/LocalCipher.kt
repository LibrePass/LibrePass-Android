package dev.medzik.librepass.android.database.tables

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
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

@Dao
interface LocalCipherDao {
    @Query("SELECT * FROM localCipher WHERE id = :id")
    fun get(id: UUID): LocalCipher?

    @Query("SELECT * FROM localCipher WHERE owner = :owner")
    fun getAll(owner: UUID): List<LocalCipher>

    @Query("SELECT id FROM localCipher WHERE owner = :owner")
    fun getAllIDs(owner: UUID): List<UUID>

    @Query("DELETE FROM localCipher WHERE id = :id")
    fun delete(id: UUID)

    @Query("DELETE FROM localCipher WHERE id IN (:ids)")
    fun delete(ids: List<UUID>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cipherTable: LocalCipher)

    @Update
    fun update(cipherTable: LocalCipher)

    @Query("DELETE FROM localCipher WHERE owner = :owner")
    suspend fun drop(owner: UUID)
}
