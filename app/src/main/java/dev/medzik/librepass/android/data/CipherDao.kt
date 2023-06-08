package dev.medzik.librepass.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

/**
 * Data access object for [CipherTable].
 */
@Dao
interface CipherDao {
    /**
     * Get a cipher by id.
     * @param id cipher identifier
     * @return The cipher with the given id.
     */
    @Query("SELECT * FROM cipherTable WHERE id = :id")
    fun get(id: UUID): CipherTable?

    /**
     * Get all ciphers.
     * @param owner user identifier
     * @return All ciphers.
     */
    @Query("SELECT * FROM cipherTable WHERE owner = :owner")
    fun getAll(owner: UUID): List<CipherTable>

    /**
     * Get all ciphers ids.
     * @param owner user identifier
     * @return All ciphers ids.
     */
    @Query("SELECT id FROM cipherTable WHERE owner = :owner")
    fun getAllIDs(owner: UUID): List<UUID>

    /**
     * Delete a cipher by id.
     * @param id cipher identifier
     */
    @Query("DELETE FROM cipherTable WHERE id = :id")
    fun delete(id: UUID)

    /**
     * Delete ciphers by ids.
     * @param ids cipher identifiers
     */
    @Query("DELETE FROM cipherTable WHERE id IN (:ids)")
    fun delete(ids: List<UUID>)

    /**
     * Insert a cipher. If the cipher already exists, replace it.
     * @param cipherTable cipher to be inserted
     * @return The id of the inserted cipher.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cipherTable: CipherTable)

    /**
     * Update a cipher.
     * @param cipherTable updated cipher
     */
    @Update
    fun update(cipherTable: CipherTable)

    /**
     * Drop all ciphers owned by the given owner.
     * @param owner user identifier
     */
    @Query("DELETE FROM cipherTable WHERE owner = :owner")
    suspend fun drop(owner: UUID)
}
