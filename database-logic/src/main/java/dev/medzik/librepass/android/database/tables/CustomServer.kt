package dev.medzik.librepass.android.database.tables

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity
data class CustomServer(
    val name: String,
    @PrimaryKey
    val address: String
)

@Dao
interface CustomServerDao {
    @Query("SELECT * FROM customserver")
    fun getAll(): List<CustomServer>

    @Insert
    suspend fun insert(customServer: CustomServer)
}
