package dev.medzik.librepass.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CredentialsDao {
    @Insert
    suspend fun insert(credentials: Credentials)

    @Query("SELECT * FROM credentials LIMIT 1")
    fun get(): Credentials?

    @Update
    suspend fun update(credentials: Credentials)

    @Query("DELETE FROM credentials")
    suspend fun drop()
}
