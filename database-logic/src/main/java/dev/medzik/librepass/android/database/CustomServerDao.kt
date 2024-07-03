package dev.medzik.librepass.android.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CustomServerDao {
    @Query("SELECT * FROM customserver")
    fun getAll(): List<CustomServer>

    @Insert
    suspend fun insert(customServer: CustomServer)
}
