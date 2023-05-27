package dev.medzik.librepass.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Data access object for [Credentials].
 */
@Dao
interface CredentialsDao {
    /**
     * Insert credentials into the database.
     * @param credentials The credentials to be inserted.
     */
    @Insert
    suspend fun insert(credentials: Credentials)

    /**
     * Get credentials from the database.
     * @return The credentials from the database, if any.
     */
    @Query("SELECT * FROM credentials LIMIT 1")
    fun get(): Credentials?

    /**
     * Update credentials in the database.
     * @param credentials The updated credentials.
     */
    @Update
    suspend fun update(credentials: Credentials)

    /**
     * Delete credentials from the database.
     */
    @Query("DELETE FROM credentials")
    suspend fun drop()
}
