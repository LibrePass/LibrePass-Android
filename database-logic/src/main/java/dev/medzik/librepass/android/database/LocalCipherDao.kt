package dev.medzik.librepass.android.database

import androidx.room.*
import java.util.*

@Dao
interface LocalCipherDao {
    /**
     * Get a cipher by id.
     * @param id cipher identifier
     * @return The cipher with the given id.
     */
    @Query("SELECT * FROM localCipher WHERE id = :id")
    fun get(id: UUID): LocalCipher?

    /**
     * Get all ciphers.
     * @param owner user identifier
     * @return All ciphers.
     */
    @Query("SELECT * FROM localCipher WHERE owner = :owner")
    fun getAll(owner: UUID): List<LocalCipher>

    /**
     * Get all ciphers ids.
     * @param owner user identifier
     * @return All ciphers ids.
     */
    @Query("SELECT id FROM localCipher WHERE owner = :owner")
    fun getAllIDs(owner: UUID): List<UUID>

    /**
     * Delete a cipher by id.
     * @param id cipher identifier
     */
    @Query("DELETE FROM localCipher WHERE id = :id")
    fun delete(id: UUID)

    /**
     * Delete ciphers by ids.
     * @param ids cipher identifiers
     */
    @Query("DELETE FROM localCipher WHERE id IN (:ids)")
    fun delete(ids: List<UUID>)

    /**
     * Insert a cipher. If the cipher already exists, replace it.
     * @param cipherTable cipher to be inserted
     * @return The id of the inserted cipher.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cipherTable: LocalCipher)

    /**
     * Update a cipher.
     * @param cipherTable updated cipher
     */
    @Update
    fun update(cipherTable: LocalCipher)

    /**
     * Drop all ciphers owned by the given owner.
     * @param owner user identifier
     */
    @Query("DELETE FROM localCipher WHERE owner = :owner")
    suspend fun drop(owner: UUID)
}