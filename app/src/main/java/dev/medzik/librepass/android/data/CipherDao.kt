package dev.medzik.librepass.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

@Dao
interface CipherDao {
    @Query("SELECT * FROM cipherTable WHERE id = :id")
    fun get(id: UUID): CipherTable?

    @Query("SELECT * FROM cipherTable WHERE owner = :owner")
    fun getAll(owner: UUID): List<CipherTable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cipherTable: CipherTable)

    @Update
    suspend fun update(cipherTable: CipherTable)

    @Query("DELETE FROM cipherTable WHERE owner = :owner")
    suspend fun drop(owner: UUID)
}
